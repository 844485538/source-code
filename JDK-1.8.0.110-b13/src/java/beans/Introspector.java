/*
 * @(#)Introspector.java	1.145 09/04/29
 *
 * Copyright 1996-2009 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package java.beans;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import java.security.AccessController;
import java.security.PrivilegedAction;

import java.util.Collections;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.EventListener;
import java.util.List;
import java.util.WeakHashMap;
import java.util.TreeMap;
import sun.awt.AppContext;
import sun.reflect.misc.ReflectUtil;

/**
 * The Introspector class provides a standard way for tools to learn about
 * the properties, events, and methods supported by a target Java Bean.
 * <p>
 * For each of those three kinds of information, the Introspector will
 * separately analyze the bean's class and superclasses looking for
 * either explicit or implicit information and use that information to
 * build a BeanInfo object that comprehensively describes the target bean.
 * <p>
 * For each class "Foo", explicit information may be available if there exists
 * a corresponding "FooBeanInfo" class that provides a non-null value when
 * queried for the information.   We first look for the BeanInfo class by
 * taking the full package-qualified name of the target bean class and
 * appending "BeanInfo" to form a new class name.  If this fails, then
 * we take the final classname component of this name, and look for that
 * class in each of the packages specified in the BeanInfo package search
 * path.
 * <p>
 * Thus for a class such as "sun.xyz.OurButton" we would first look for a
 * BeanInfo class called "sun.xyz.OurButtonBeanInfo" and if that failed we'd
 * look in each package in the BeanInfo search path for an OurButtonBeanInfo
 * class.  With the default search path, this would mean looking for
 * "sun.beans.infos.OurButtonBeanInfo".
 * <p>
 * If a class provides explicit BeanInfo about itself then we add that to
 * the BeanInfo information we obtained from analyzing any derived classes,
 * but we regard the explicit information as being definitive for the current
 * class and its base classes, and do not proceed any further up the superclass
 * chain.
 * <p>
 * If we don't find explicit BeanInfo on a class, we use low-level
 * reflection to study the methods of the class and apply standard design
 * patterns to identify property accessors, event sources, or public
 * methods.  We then proceed to analyze the class's superclass and add
 * in the information from it (and possibly on up the superclass chain).
 *
 * <p>
 * Because the Introspector caches BeanInfo classes for better performance, 
 * take care if you use it in an application that uses
 * multiple class loaders.
 * In general, when you destroy a <code>ClassLoader</code>
 * that has been used to introspect classes,
 * you should use the
 * {@link #flushCaches <code>Introspector.flushCaches</code>} 
 * or 
 * {@link #flushFromCaches <code>Introspector.flushFromCaches</code>} method
 * to flush all of the introspected classes out of the cache.
 *
 * <P>
 * For more information about introspection and design patterns, please 
 * consult the 
 *  <a href="http://java.sun.com/products/javabeans/docs/index.html">JavaBeans specification</a>.
 */

public class Introspector {

    // Flags that can be used to control getBeanInfo:
    public final static int USE_ALL_BEANINFO           = 1;
    public final static int IGNORE_IMMEDIATE_BEANINFO  = 2;
    public final static int IGNORE_ALL_BEANINFO        = 3;

    // Static Caches to speed up introspection.
    private static Map declaredMethodCache = 
	Collections.synchronizedMap(new WeakHashMap());

    private static final Object BEANINFO_CACHE = new Object();

    private Class beanClass;
    private BeanInfo explicitBeanInfo;
    private BeanInfo superBeanInfo;
    private BeanInfo additionalBeanInfo[];

    private boolean propertyChangeSource = false;
    private static Class eventListenerType = EventListener.class;

    // These should be removed.
    private String defaultEventName;
    private String defaultPropertyName;
    private int defaultEventIndex = -1;
    private int defaultPropertyIndex = -1;

    // Methods maps from Method objects to MethodDescriptors
    private Map methods;

    // properties maps from String names to PropertyDescriptors
    private Map properties;

    // events maps from String names to EventSetDescriptors
    private Map events;

    private final static String DEFAULT_INFO_PATH = "sun.beans.infos";

    private static String[] searchPath = { DEFAULT_INFO_PATH };

    private final static EventSetDescriptor[] EMPTY_EVENTSETDESCRIPTORS = new EventSetDescriptor[0];

    private static final String ADD_PREFIX = "add";
    private static final String REMOVE_PREFIX = "remove";
    private static final String GET_PREFIX = "get";
    private static final String SET_PREFIX = "set";
    private static final String IS_PREFIX = "is";
    private static final String BEANINFO_SUFFIX = "BeanInfo";

    //======================================================================
    // 				Public methods
    //======================================================================

    /**
     * Introspect on a Java Bean and learn about all its properties, exposed
     * methods, and events.
     * <p>
     * If the BeanInfo class for a Java Bean has been previously Introspected
     * then the BeanInfo class is retrieved from the BeanInfo cache.
     *
     * @param beanClass  The bean class to be analyzed.
     * @return  A BeanInfo object describing the target bean.
     * @exception IntrospectionException if an exception occurs during
     *              introspection.
     * @see #flushCaches
     * @see #flushFromCaches
     */
    public static BeanInfo getBeanInfo(Class<?> beanClass)
	throws IntrospectionException
    {
	if (!ReflectUtil.isPackageAccessible(beanClass)) {
	    return (new Introspector(beanClass, null, USE_ALL_BEANINFO)).getBeanInfo();
	}
        Map<Class<?>, BeanInfo> map;
        synchronized (BEANINFO_CACHE) {
            map = (Map<Class<?>, BeanInfo>) AppContext.getAppContext().get(BEANINFO_CACHE);
            if (map == null) {
                map = Collections.synchronizedMap(new WeakHashMap<Class<?>, BeanInfo>());
                AppContext.getAppContext().put(BEANINFO_CACHE, map);
            }
        }
        BeanInfo bi = map.get(beanClass);
	if (bi == null) {
	    bi = (new Introspector(beanClass, null, USE_ALL_BEANINFO)).getBeanInfo();
	    map.put(beanClass, bi);
	}
	return bi;
    }

    /**
     * Introspect on a Java bean and learn about all its properties, exposed
     * methods, and events, subject to some control flags.
     * <p>
     * If the BeanInfo class for a Java Bean has been previously Introspected
     * based on the same arguments then the BeanInfo class is retrieved 
     * from the BeanInfo cache.
     *
     * @param beanClass  The bean class to be analyzed.
     * @param flags  Flags to control the introspection.
     *     If flags == USE_ALL_BEANINFO then we use all of the BeanInfo
     *	 	classes we can discover.
     *     If flags == IGNORE_IMMEDIATE_BEANINFO then we ignore any
     *           BeanInfo associated with the specified beanClass.
     *     If flags == IGNORE_ALL_BEANINFO then we ignore all BeanInfo
     *           associated with the specified beanClass or any of its
     *		 parent classes.
     * @return  A BeanInfo object describing the target bean.
     * @exception IntrospectionException if an exception occurs during
     *              introspection.
     */
    public static BeanInfo getBeanInfo(Class<?> beanClass, int flags)
						throws IntrospectionException {
	return getBeanInfo(beanClass, null, flags);
    }

    /**
     * Introspect on a Java bean and learn all about its properties, exposed
     * methods, below a given "stop" point.
     * <p>
     * If the BeanInfo class for a Java Bean has been previously Introspected
     * based on the same arguments, then the BeanInfo class is retrieved
     * from the BeanInfo cache.
     *
     * @param beanClass The bean class to be analyzed.
     * @param stopClass The baseclass at which to stop the analysis.  Any
     *    methods/properties/events in the stopClass or in its baseclasses
     *    will be ignored in the analysis.
     * @exception IntrospectionException if an exception occurs during
     *              introspection.
     */
    public static BeanInfo getBeanInfo(Class<?> beanClass, Class<?> stopClass)
						throws IntrospectionException {
	return getBeanInfo(beanClass, stopClass, USE_ALL_BEANINFO);
    }

    /**
     * Only called from the public getBeanInfo methods. This method caches
     * the Introspected BeanInfo based on the arguments.
     */
    private static BeanInfo getBeanInfo(Class beanClass, Class stopClass, 
					int flags) throws IntrospectionException {
	BeanInfo bi;	
	if (stopClass == null && flags == USE_ALL_BEANINFO) {
	    // Same parameters to take advantage of caching.
	    bi = getBeanInfo(beanClass);
	} else {
	    bi = (new Introspector(beanClass, stopClass, flags)).getBeanInfo();
	}
	return bi;

	// Old behaviour: Make an independent copy of the BeanInfo.
	//return new GenericBeanInfo(bi);
    }


    /**
     * Utility method to take a string and convert it to normal Java variable
     * name capitalization.  This normally means converting the first
     * character from upper case to lower case, but in the (unusual) special
     * case when there is more than one character and both the first and
     * second characters are upper case, we leave it alone.
     * <p>
     * Thus "FooBah" becomes "fooBah" and "X" becomes "x", but "URL" stays
     * as "URL".
     *
     * @param  name The string to be decapitalized.
     * @return  The decapitalized version of the string.
     */
    public static String decapitalize(String name) {
	if (name == null || name.length() == 0) {
	    return name;
	}
	if (name.length() > 1 && Character.isUpperCase(name.charAt(1)) &&
			Character.isUpperCase(name.charAt(0))){
	    return name;
	}
	char chars[] = name.toCharArray();
	chars[0] = Character.toLowerCase(chars[0]);
	return new String(chars);
    }

    /**
     * Gets the list of package names that will be used for
     *		finding BeanInfo classes.
     *
     * @return  The array of package names that will be searched in
     *		order to find BeanInfo classes. The default value
     *          for this array is implementation-dependent; e.g. 
     *          Sun implementation initially sets to {"sun.beans.infos"}.
     */

    public static synchronized String[] getBeanInfoSearchPath() {
	// Return a copy of the searchPath.
	String result[] = new String[searchPath.length];
	for (int i = 0; i < searchPath.length; i++) {
	    result[i] = searchPath[i];
	}
	return result;
    }

    /**
     * Change the list of package names that will be used for
     *		finding BeanInfo classes.  The behaviour of 
     *          this method is undefined if parameter path
     *          is null.
     * 
     * <p>First, if there is a security manager, its <code>checkPropertiesAccess</code> 
     * method is called. This could result in a SecurityException.
     * 
     * @param path  Array of package names.
     * @exception  SecurityException  if a security manager exists and its  
     *             <code>checkPropertiesAccess</code> method doesn't allow setting
     *              of system properties.
     * @see SecurityManager#checkPropertiesAccess
     */

    public static synchronized void setBeanInfoSearchPath(String path[]) {
	SecurityManager sm = System.getSecurityManager();
	if (sm != null) {
	    sm.checkPropertiesAccess();
	}
	searchPath = path;
    }


    /**
     * Flush all of the Introspector's internal caches.  This method is
     * not normally required.  It is normally only needed by advanced
     * tools that update existing "Class" objects in-place and need
     * to make the Introspector re-analyze existing Class objects.
     */

    public static void flushCaches() {
        Map map = (Map) AppContext.getAppContext().get(BEANINFO_CACHE);
        if (map != null) {
            map.clear();
        }
	declaredMethodCache.clear();
    }

    /**
     * Flush the Introspector's internal cached information for a given class.
     * This method is not normally required.  It is normally only needed
     * by advanced tools that update existing "Class" objects in-place
     * and need to make the Introspector re-analyze an existing Class object.
     *
     * Note that only the direct state associated with the target Class
     * object is flushed.  We do not flush state for other Class objects
     * with the same name, nor do we flush state for any related Class
     * objects (such as subclasses), even though their state may include
     * information indirectly obtained from the target Class object.
     *
     * @param clz  Class object to be flushed.
     * @throws NullPointerException If the Class object is null.
     */
    public static void flushFromCaches(Class<?> clz) {
	if (clz == null) {
	    throw new NullPointerException();
	}
        Map map = (Map) AppContext.getAppContext().get(BEANINFO_CACHE);
        if (map != null) {
            map.remove(clz);
        }
	declaredMethodCache.remove(clz);
    }

    //======================================================================
    // 			Private implementation methods
    //======================================================================

    private Introspector(Class beanClass, Class stopClass, int flags)
					    throws IntrospectionException {
	this.beanClass = beanClass;

	// Check stopClass is a superClass of startClass.
	if (stopClass != null) {
	    boolean isSuper = false;
	    for (Class c = beanClass.getSuperclass(); c != null; c = c.getSuperclass()) {
	        if (c == stopClass) {
		    isSuper = true;
	        }
	    }
	    if (!isSuper) {
	        throw new IntrospectionException(stopClass.getName() + " not superclass of " + 
					beanClass.getName());
	    }
	}

        if (flags == USE_ALL_BEANINFO) {
	    explicitBeanInfo = findExplicitBeanInfo(beanClass);
        }

	Class superClass = beanClass.getSuperclass();
	if (superClass != stopClass) {
	    int newFlags = flags;
	    if (newFlags == IGNORE_IMMEDIATE_BEANINFO) {
		newFlags = USE_ALL_BEANINFO;
	    }
	    superBeanInfo = getBeanInfo(superClass, stopClass, newFlags);
	}
	if (explicitBeanInfo != null) {
	    additionalBeanInfo = explicitBeanInfo.getAdditionalBeanInfo();
	} 
	if (additionalBeanInfo == null) {
	    additionalBeanInfo = new BeanInfo[0];
	}
    }

    /**
     * Constructs a GenericBeanInfo class from the state of the Introspector
     */
    private BeanInfo getBeanInfo() throws IntrospectionException {

	// the evaluation order here is import, as we evaluate the
	// event sets and locate PropertyChangeListeners before we
	// look for properties.
	BeanDescriptor bd = getTargetBeanDescriptor();
	MethodDescriptor mds[] = getTargetMethodInfo();
	EventSetDescriptor esds[] = getTargetEventInfo();
	PropertyDescriptor pds[] = getTargetPropertyInfo();

	int defaultEvent = getTargetDefaultEventIndex();
	int defaultProperty = getTargetDefaultPropertyIndex();

        return new GenericBeanInfo(bd, esds, defaultEvent, pds,
			defaultProperty, mds, explicitBeanInfo);
	
    }

    /**
     * Looks for an explicit BeanInfo class that corresponds to the Class.
     * First it looks in the existing package that the Class is defined in,
     * then it checks to see if the class is its own BeanInfo. Finally,
     * the BeanInfo search path is prepended to the class and searched.
     *
     * @return Instance of an explicit BeanInfo class or null if one isn't found.
     */
    private static synchronized BeanInfo findExplicitBeanInfo(Class beanClass) {
	String name = beanClass.getName() + BEANINFO_SUFFIX;
        try {
	    return (java.beans.BeanInfo)instantiate(beanClass, name);
	} catch (Exception ex) {
	    // Just drop through

        }
	// Now try checking if the bean is its own BeanInfo.
        try {
	    if (isSubclass(beanClass, java.beans.BeanInfo.class)) {
	        return (java.beans.BeanInfo)beanClass.newInstance();
	    }
	} catch (Exception ex) {
	    // Just drop through
        }
	// Now try looking for <searchPath>.fooBeanInfo
 	name = name.substring(name.lastIndexOf('.')+1);

	for (int i = 0; i < searchPath.length; i++) {
	    // This optimization will only use the BeanInfo search path if is has changed
	    // from the original or trying to get the ComponentBeanInfo. 
	    if (!DEFAULT_INFO_PATH.equals(searchPath[i]) || 
		DEFAULT_INFO_PATH.equals(searchPath[i]) && "ComponentBeanInfo".equals(name)) {
		try {
		    String fullName = searchPath[i] + "." + name;
		    java.beans.BeanInfo bi = (java.beans.BeanInfo)instantiate(beanClass, fullName);

		    // Make sure that the returned BeanInfo matches the class.
		    if (bi.getBeanDescriptor() != null) {
			if (bi.getBeanDescriptor().getBeanClass() == beanClass) {
			    return bi;
			}
		    } else if (bi.getPropertyDescriptors() != null) {
			PropertyDescriptor[] pds = bi.getPropertyDescriptors();
			for (int j = 0; j < pds.length; j++) {
			    Method method = pds[j].getReadMethod();
			    if (method == null) {
				method = pds[j].getWriteMethod();
			    }
			    if (method != null && method.getDeclaringClass() == beanClass) {
				return bi;
			    }
			}
		    } else if (bi.getMethodDescriptors() != null) {
			MethodDescriptor[] mds = bi.getMethodDescriptors();
			for (int j = 0; j < mds.length; j++) {
			    Method method = mds[j].getMethod();
			    if (method != null && method.getDeclaringClass() == beanClass) {
				return bi;
			    }
			}
		    }
		} catch (Exception ex) {
		    // Silently ignore any errors.
		}
	    }
	}
	return null;
    }

    /**
     * @return An array of PropertyDescriptors describing the editable
     * properties supported by the target bean.
     */

    private PropertyDescriptor[] getTargetPropertyInfo() {

	// Check if the bean has its own BeanInfo that will provide
	// explicit information.
        PropertyDescriptor[] explicitProperties = null;
	if (explicitBeanInfo != null) {
	    explicitProperties = explicitBeanInfo.getPropertyDescriptors();
	    int ix = explicitBeanInfo.getDefaultPropertyIndex();
	    if (ix >= 0 && ix < explicitProperties.length) {
		defaultPropertyName = explicitProperties[ix].getName();
	    }
        }

	if (explicitProperties == null && superBeanInfo != null) {
	    // We have no explicit BeanInfo properties.  Check with our parent.
	    PropertyDescriptor supers[] = superBeanInfo.getPropertyDescriptors();
	    for (int i = 0 ; i < supers.length; i++) {
		addPropertyDescriptor(supers[i]);
	    }
	    int ix = superBeanInfo.getDefaultPropertyIndex();
	    if (ix >= 0 && ix < supers.length) {
		defaultPropertyName = supers[ix].getName();
	    }
	}

	for (int i = 0; i < additionalBeanInfo.length; i++) {
	    PropertyDescriptor additional[] = additionalBeanInfo[i].getPropertyDescriptors();
	    if (additional != null) {
	        for (int j = 0 ; j < additional.length; j++) {
		    addPropertyDescriptor(additional[j]);
	        }
	    }
	}

	if (explicitProperties != null) {
	    // Add the explicit BeanInfo data to our results.
	    for (int i = 0 ; i < explicitProperties.length; i++) {
		addPropertyDescriptor(explicitProperties[i]);
	    }

	} else {

	    // Apply some reflection to the current class.

	    // First get an array of all the public methods at this level
	    Method methodList[] = getPublicDeclaredMethods(beanClass);

	    // Now analyze each method.
	    for (int i = 0; i < methodList.length; i++) {
	        Method method = methodList[i];
		if (method == null) {
		    continue;
		}
	        // skip static methods.
		int mods = method.getModifiers();
		if (Modifier.isStatic(mods)) {
		    continue;
		}
	        String name = method.getName();
	        Class argTypes[] = method.getParameterTypes();
	        Class resultType = method.getReturnType();
		int argCount = argTypes.length;
		PropertyDescriptor pd = null;

		if (name.length() <= 3 && !name.startsWith(IS_PREFIX)) {
		    // Optimization. Don't bother with invalid propertyNames.
		    continue;
		}

		try {

	            if (argCount == 0) {
		        if (name.startsWith(GET_PREFIX)) {
		            // Simple getter
	                    pd = new PropertyDescriptor(decapitalize(name.substring(3)),
						method, null);
	                } else if (resultType == boolean.class && name.startsWith(IS_PREFIX)) {
		            // Boolean getter
	                    pd = new PropertyDescriptor(decapitalize(name.substring(2)),
						method, null);
		        }
	            } else if (argCount == 1) {
		        if (argTypes[0] == int.class && name.startsWith(GET_PREFIX)) {
		            pd = new IndexedPropertyDescriptor(
						decapitalize(name.substring(3)),
						null, null,
						method,	null);
		        } else if (resultType == void.class && name.startsWith(SET_PREFIX)) {
		            // Simple setter
	                    pd = new PropertyDescriptor(decapitalize(name.substring(3)),
						null, method);
		            if (throwsException(method, PropertyVetoException.class)) {
			        pd.setConstrained(true);
			    }			
		        }
	            } else if (argCount == 2) {
			    if (argTypes[0] == int.class && name.startsWith(SET_PREFIX)) {
	                    pd = new IndexedPropertyDescriptor(
						decapitalize(name.substring(3)),
						null, null,
						null, method);
		            if (throwsException(method, PropertyVetoException.class)) {
			        pd.setConstrained(true);			
			    }
			}
		    }
		} catch (IntrospectionException ex) {
		    // This happens if a PropertyDescriptor or IndexedPropertyDescriptor
	            // constructor fins that the method violates details of the deisgn
		    // pattern, e.g. by having an empty name, or a getter returning
		    // void , or whatever.
		    pd = null;
		}

		if (pd != null) {
		    // If this class or one of its base classes is a PropertyChange
		    // source, then we assume that any properties we discover are "bound".
		    if (propertyChangeSource) {
			pd.setBound(true);
		    }
		    addPropertyDescriptor(pd);
		}
	    }
	}
	processPropertyDescriptors();

	// Allocate and populate the result array.
	PropertyDescriptor result[] = new PropertyDescriptor[properties.size()];
	result = (PropertyDescriptor[])properties.values().toArray(result);

	// Set the default index. 
	if (defaultPropertyName != null) {
	    for (int i = 0; i < result.length; i++) {
		if (defaultPropertyName.equals(result[i].getName())) {
		    defaultPropertyIndex = i;
		}
	    }
	}

	return result;
    }

    private HashMap pdStore = new HashMap();

    /**
     * Adds the property descriptor to the list store.
     */
    private void addPropertyDescriptor(PropertyDescriptor pd) {
	String propName = pd.getName();
	List list = (List)pdStore.get(propName);
	if (list == null) {
	    list = new ArrayList();
	    pdStore.put(propName, list);
	}
	list.add(pd);
    }

    /**
     * Populates the property descriptor table by merging the 
     * lists of Property descriptors.
     */ 
    private void processPropertyDescriptors() {
	if (properties == null) {
	    properties = new TreeMap();
	}

	List list;

	PropertyDescriptor pd, gpd, spd;
	IndexedPropertyDescriptor ipd, igpd, ispd;

	Iterator it = pdStore.values().iterator();
	while (it.hasNext()) {
	    pd = null; gpd = null; spd = null; 
	    ipd = null; igpd = null; ispd = null;

	    list = (List)it.next();

	    // First pass. Find the latest getter method. Merge properties
	    // of previous getter methods.
	    for (int i = 0; i < list.size(); i++) {
		pd = (PropertyDescriptor)list.get(i);
		if (pd instanceof IndexedPropertyDescriptor) {
		    ipd = (IndexedPropertyDescriptor)pd;
		    if (ipd.getIndexedReadMethod() != null) {
			if (igpd != null) {
			    igpd = new IndexedPropertyDescriptor(igpd, ipd);
			} else {
			    igpd = ipd;
			}
		    }
		} else {
		    if (pd.getReadMethod() != null) {
			if (gpd != null) {
			    // Don't replace the existing read
			    // method if it starts with "is"
			    Method method = gpd.getReadMethod();
			    if (!method.getName().startsWith(IS_PREFIX)) {
				gpd = new PropertyDescriptor(gpd, pd);
			    }
			} else {
			    gpd = pd;
			}
		    }
		}
	    }

	    // Second pass. Find the latest setter method which
	    // has the same type as the getter method.
	    for (int i = 0; i < list.size(); i++) {
		pd = (PropertyDescriptor)list.get(i);
		if (pd instanceof IndexedPropertyDescriptor) {
		    ipd = (IndexedPropertyDescriptor)pd;
		    if (ipd.getIndexedWriteMethod() != null) {
			if (igpd != null) {
			    if (igpd.getIndexedPropertyType() 
				== ipd.getIndexedPropertyType()) {
				if (ispd != null) {
				    ispd = new IndexedPropertyDescriptor(ispd, ipd);
				} else {
				    ispd = ipd;
				}
			    }
			} else {
			    if (ispd != null) {
				ispd = new IndexedPropertyDescriptor(ispd, ipd);
			    } else {
				ispd = ipd;
			    }
			}
		    }
		} else {
		    if (pd.getWriteMethod() != null) {
			if (gpd != null) {
			    if (gpd.getPropertyType() == pd.getPropertyType()) {
				if (spd != null) {
				    spd = new PropertyDescriptor(spd, pd);
				} else {
				    spd = pd;
				}
			    }
			} else {
			    if (spd != null) {
				spd = new PropertyDescriptor(spd, pd);
			    } else {
				spd = pd;
			    }
			}
		    }
		}
	    }

	    // At this stage we should have either PDs or IPDs for the
	    // representative getters and setters. The order at which the 
	    // property descriptors are determined represent the 
	    // precedence of the property ordering.
	    pd = null; ipd = null;

	    if (igpd != null && ispd != null) {
		// Complete indexed properties set
		// Merge any classic property descriptors
		if (gpd != null) {
		    PropertyDescriptor tpd = mergePropertyDescriptor(igpd, gpd);
		    if (tpd instanceof IndexedPropertyDescriptor) {
			igpd = (IndexedPropertyDescriptor)tpd;
		    }
		}
		if (spd != null) {
		    PropertyDescriptor tpd = mergePropertyDescriptor(ispd, spd);
		    if (tpd instanceof IndexedPropertyDescriptor) {
			ispd = (IndexedPropertyDescriptor)tpd;
		    }
		}
		if (igpd == ispd) {
		    pd = igpd;
		} else {
		    pd = mergePropertyDescriptor(igpd, ispd);
		}
	    } else if (gpd != null && spd != null) {
		// Complete simple properties set
		if (gpd == spd) {
		    pd = gpd;
		} else {
		    pd = mergePropertyDescriptor(gpd, spd);
		}
	    } else if (ispd != null) {
		// indexed setter
		pd = ispd;
		// Merge any classic property descriptors
		if (spd != null) {
		    pd = mergePropertyDescriptor(ispd, spd);
		}
		if (gpd != null) {
		    pd = mergePropertyDescriptor(ispd, gpd);
		}
	    } else if (igpd != null) {
		// indexed getter
		pd = igpd;
		// Merge any classic property descriptors
		if (gpd != null) {
		    pd = mergePropertyDescriptor(igpd, gpd);
		}
		if (spd != null) {
		    pd = mergePropertyDescriptor(igpd, spd);
		}
	    } else if (spd != null) {
		// simple setter
		pd = spd;
	    } else if (gpd != null) {
		// simple getter
		pd = gpd;
	    }

	    // Very special case to ensure that an IndexedPropertyDescriptor
	    // doesn't contain less information than the enclosed 
	    // PropertyDescriptor. If it does, then recreate as a 
	    // PropertyDescriptor. See 4168833
	    if (pd instanceof IndexedPropertyDescriptor) {
		ipd = (IndexedPropertyDescriptor)pd;
		if (ipd.getIndexedReadMethod() == null && ipd.getIndexedWriteMethod() == null) {
		    pd = new PropertyDescriptor(ipd);
		}
	    }

            // Find the first property descriptor
            // which does not have getter and setter methods.
            // See regression bug 4984912.
            if ( (pd == null) && (list.size() > 0) ) {
                pd = (PropertyDescriptor) list.get(0);
            }

	    if (pd != null) {
		properties.put(pd.getName(), pd);
	    }
	}
    }
    
    /**
     * Adds the property descriptor to the indexedproperty descriptor only if the 
     * types are the same.
     *
     * The most specific property descriptor will take precedence.
     */
    private PropertyDescriptor mergePropertyDescriptor(IndexedPropertyDescriptor ipd, 
						       PropertyDescriptor pd) {
	PropertyDescriptor result = null;

	Class propType = pd.getPropertyType();
	Class ipropType = ipd.getIndexedPropertyType();

	if (propType.isArray() && propType.getComponentType() == ipropType) {
	    if (pd.getClass0().isAssignableFrom(ipd.getClass0())) {
		result = new IndexedPropertyDescriptor(pd, ipd);
	    } else {
		result = new IndexedPropertyDescriptor(ipd, pd);
	    }
	} else {
	    // Cannot merge the pd because of type mismatch
	    // Return the most specific pd
	    if (pd.getClass0().isAssignableFrom(ipd.getClass0())) {
		result = ipd;
	    } else {
		result = pd;
		// Try to add methods which may have been lost in the type change
		// See 4168833
		Method write = result.getWriteMethod();
		Method read = result.getReadMethod();
		    		    
		if (read == null && write != null) {
		    read = findMethod(result.getClass0(), 
				      "get" + result.capitalize(result.getName()), 0);
		    if (read != null) {
			try {
			    result.setReadMethod(read);
			} catch (IntrospectionException ex) {
			    // no consequences for failure.
			}
		    }
		}
		if (write == null && read != null) {
		    write = findMethod(result.getClass0(), 
				       "set" + result.capitalize(result.getName()), 1,
				       new Class[] { read.getReturnType() });
		    if (write != null) {
			try {
			    result.setWriteMethod(write);
			} catch (IntrospectionException ex) {
			    // no consequences for failure.
			}
		    }
		}
	    }
	}
	return result;
    }

    // Handle regular pd merge
    private PropertyDescriptor mergePropertyDescriptor(PropertyDescriptor pd1,
						       PropertyDescriptor pd2) {
	if (pd1.getClass0().isAssignableFrom(pd2.getClass0())) {
	    return new PropertyDescriptor(pd1, pd2);
	} else {
	    return new PropertyDescriptor(pd2, pd1);
	}
    }

    // Handle regular ipd merge
    private PropertyDescriptor mergePropertyDescriptor(IndexedPropertyDescriptor ipd1,
						       IndexedPropertyDescriptor ipd2) {
	if (ipd1.getClass0().isAssignableFrom(ipd2.getClass0())) {
	    return new IndexedPropertyDescriptor(ipd1, ipd2);
	} else {
	    return new IndexedPropertyDescriptor(ipd2, ipd1);
	}
    }

    /**
     * @return An array of EventSetDescriptors describing the kinds of 
     * events fired by the target bean.
     */
    private EventSetDescriptor[] getTargetEventInfo() throws IntrospectionException {
	if (events == null) {
	    events = new HashMap();
	}

	// Check if the bean has its own BeanInfo that will provide
	// explicit information.
        EventSetDescriptor[] explicitEvents = null;
	if (explicitBeanInfo != null) {
	    explicitEvents = explicitBeanInfo.getEventSetDescriptors();
	    int ix = explicitBeanInfo.getDefaultEventIndex();
	    if (ix >= 0 && ix < explicitEvents.length) {
		defaultEventName = explicitEvents[ix].getName();
	    }
	}

	if (explicitEvents == null && superBeanInfo != null) {
	    // We have no explicit BeanInfo events.  Check with our parent.
	    EventSetDescriptor supers[] = superBeanInfo.getEventSetDescriptors();
	    for (int i = 0 ; i < supers.length; i++) {
		addEvent(supers[i]);
	    }
	    int ix = superBeanInfo.getDefaultEventIndex();
	    if (ix >= 0 && ix < supers.length) {
		defaultEventName = supers[ix].getName();
	    }
	}

	for (int i = 0; i < additionalBeanInfo.length; i++) {
	    EventSetDescriptor additional[] = additionalBeanInfo[i].getEventSetDescriptors();
	    if (additional != null) {
	        for (int j = 0 ; j < additional.length; j++) {
		    addEvent(additional[j]);
	        }
	    }
	}

	if (explicitEvents != null) {
	    // Add the explicit explicitBeanInfo data to our results.
	    for (int i = 0 ; i < explicitEvents.length; i++) {
		addEvent(explicitEvents[i]);
	    }

	} else {

	    // Apply some reflection to the current class.

	    // Get an array of all the public beans methods at this level
	    Method methodList[] = getPublicDeclaredMethods(beanClass);

	    // Find all suitable "add", "remove" and "get" Listener methods
	    // The name of the listener type is the key for these hashtables
	    // i.e, ActionListener
	    Map adds = null;
	    Map removes = null;
	    Map gets = null;

	    for (int i = 0; i < methodList.length; i++) {
	        Method method = methodList[i];
		if (method == null) {
		    continue;
		}
	        // skip static methods.
		int mods = method.getModifiers();
		if (Modifier.isStatic(mods)) {
		    continue;
		}
	        String name = method.getName();
		// Optimization avoid getParameterTypes
		if (!name.startsWith(ADD_PREFIX) && !name.startsWith(REMOVE_PREFIX) 
		    && !name.startsWith(GET_PREFIX)) {
		    continue;
		}

	        Class argTypes[] = method.getParameterTypes();
	        Class resultType = method.getReturnType();

	        if (name.startsWith(ADD_PREFIX) && argTypes.length == 1 &&
		    resultType == Void.TYPE &&
		    Introspector.isSubclass(argTypes[0], eventListenerType)) {
		    String listenerName = name.substring(3);
		    if (listenerName.length() > 0 && 
			argTypes[0].getName().endsWith(listenerName)) {
			if (adds == null) {
			    adds = new HashMap();
			}
			adds.put(listenerName, method);
		    }
		}
		else if (name.startsWith(REMOVE_PREFIX) && argTypes.length == 1 &&
			 resultType == Void.TYPE &&
			 Introspector.isSubclass(argTypes[0], eventListenerType)) {
		    String listenerName = name.substring(6);
		    if (listenerName.length() > 0 && 
			argTypes[0].getName().endsWith(listenerName)) {
			if (removes == null) {
			    removes = new HashMap();
			}
			removes.put(listenerName, method);
		    }
	        } 
		else if (name.startsWith(GET_PREFIX) && argTypes.length == 0 &&
			 resultType.isArray() && 
			 Introspector.isSubclass(resultType.getComponentType(), 
						 eventListenerType)) {
		    String listenerName  = name.substring(3, name.length() - 1);
		    if (listenerName.length() > 0 && 
			resultType.getComponentType().getName().endsWith(listenerName)) {
			if (gets == null) {
			    gets = new HashMap();
			}
			gets.put(listenerName, method);
		    }
		}
	    }

	    if (adds != null && removes != null) {
		// Now look for matching addFooListener+removeFooListener pairs.
		// Bonus if there is a matching getFooListeners method as well.
		Iterator keys = adds.keySet().iterator();
		while (keys.hasNext()) {
		    String listenerName = (String) keys.next();
		    // Skip any "add" which doesn't have a matching "remove" or
		    // a listener name that doesn't end with Listener
		    if (removes.get(listenerName) == null || !listenerName.endsWith("Listener")) {
			continue;
		    } 
		    String eventName = decapitalize(listenerName.substring(0, listenerName.length()-8));
		    Method addMethod = (Method)adds.get(listenerName);
		    Method removeMethod = (Method)removes.get(listenerName);
		    Method getMethod = null;
		    if (gets != null) {
			getMethod = (Method)gets.get(listenerName);
		    }
		    Class argType = addMethod.getParameterTypes()[0];
		    
		    // generate a list of Method objects for each of the target methods:
		    Method allMethods[] = getPublicDeclaredMethods(argType);
		    List validMethods = new ArrayList(allMethods.length);
		    for (int i = 0; i < allMethods.length; i++) {
			if (allMethods[i] == null) {
			    continue;
			}
		    
			if (isEventHandler(allMethods[i])) {
			    validMethods.add(allMethods[i]);
			}
		    }
		    Method[] methods = (Method[])validMethods.toArray(new Method[validMethods.size()]);

		    EventSetDescriptor esd = new EventSetDescriptor(eventName, argType,
								    methods, addMethod, 
								    removeMethod, 
								    getMethod);

		    // If the adder method throws the TooManyListenersException then it
		    // is a Unicast event source.
		    if (throwsException(addMethod,
					java.util.TooManyListenersException.class)) {
			esd.setUnicast(true);
		    }
		    addEvent(esd);
		}
	    } // if (adds != null ...
	}
	EventSetDescriptor[] result;
	if (events.size() == 0) {
	    result = EMPTY_EVENTSETDESCRIPTORS;
	} else {
	    // Allocate and populate the result array.
	    result = new EventSetDescriptor[events.size()];
	    result = (EventSetDescriptor[])events.values().toArray(result);

	    // Set the default index. 
	    if (defaultEventName != null) {
		for (int i = 0; i < result.length; i++) {
		    if (defaultEventName.equals(result[i].getName())) {
			defaultEventIndex = i;
		    }
		}
	    }
	}
	return result;
    }

    private void addEvent(EventSetDescriptor esd) {
	String key = esd.getName();
	if (esd.getName().equals("propertyChange")) {
	    propertyChangeSource = true;
	}
	EventSetDescriptor old = (EventSetDescriptor)events.get(key);
	if (old == null) {
	    events.put(key, esd);
	    return;
	}
	EventSetDescriptor composite = new EventSetDescriptor(old, esd);
	events.put(key, composite);
    }

    /**
     * @return An array of MethodDescriptors describing the private
     * methods supported by the target bean.
     */
    private MethodDescriptor[] getTargetMethodInfo() {
	if (methods == null) {
	    methods = new HashMap(100);
	}

	// Check if the bean has its own BeanInfo that will provide
	// explicit information.
        MethodDescriptor[] explicitMethods = null;
	if (explicitBeanInfo != null) {
	    explicitMethods = explicitBeanInfo.getMethodDescriptors();
	}

	if (explicitMethods == null && superBeanInfo != null) {
	    // We have no explicit BeanInfo methods.  Check with our parent.
	    MethodDescriptor supers[] = superBeanInfo.getMethodDescriptors();
	    for (int i = 0 ; i < supers.length; i++) {
		addMethod(supers[i]);
	    }
	}

	for (int i = 0; i < additionalBeanInfo.length; i++) {
	    MethodDescriptor additional[] = additionalBeanInfo[i].getMethodDescriptors();
	    if (additional != null) {
	        for (int j = 0 ; j < additional.length; j++) {
		    addMethod(additional[j]);
	        }
	    }
	}

	if (explicitMethods != null) {
	    // Add the explicit explicitBeanInfo data to our results.
	    for (int i = 0 ; i < explicitMethods.length; i++) {
		addMethod(explicitMethods[i]);
	    }

	} else {

	    // Apply some reflection to the current class.

	    // First get an array of all the beans methods at this level
	    Method methodList[] = getPublicDeclaredMethods(beanClass);

	    // Now analyze each method.
	    for (int i = 0; i < methodList.length; i++) {
	        Method method = methodList[i];
		if (method == null) {
		    continue;
		}
		MethodDescriptor md = new MethodDescriptor(method);
		addMethod(md);
	    }
	}

	// Allocate and populate the result array.
	MethodDescriptor result[] = new MethodDescriptor[methods.size()];
	result = (MethodDescriptor[])methods.values().toArray(result);

	return result;
    }

    private void addMethod(MethodDescriptor md) {
	// We have to be careful here to distinguish method by both name
	// and argument lists.
	// This method gets called a *lot, so we try to be efficient.
	String name = md.getName();

	MethodDescriptor old = (MethodDescriptor)methods.get(name);
	if (old == null) {
	    // This is the common case.
	    methods.put(name, md);
	    return;
	}	

	// We have a collision on method names.  This is rare.

	// Check if old and md have the same type.
	String[] p1 = md.getParamNames();
	String[] p2 = old.getParamNames();

	boolean match = false;
	if (p1.length == p2.length) {
	    match = true;
	    for (int i = 0; i < p1.length; i++) {
		if (p1[i] != p2[i]) {
		    match = false;
		    break;
		}
	    }
	}
	if (match) {
	    MethodDescriptor composite = new MethodDescriptor(old, md);
	    methods.put(name, composite);
	    return;
	}

	// We have a collision on method names with different type signatures.
	// This is very rare.

	String longKey = makeQualifiedMethodName(name, p1);
	old = (MethodDescriptor)methods.get(longKey);
	if (old == null) {
	    methods.put(longKey, md);
	    return;
	}	
	MethodDescriptor composite = new MethodDescriptor(old, md);
	methods.put(longKey, composite);
    }

    /**
     * Creates a key for a method in a method cache.
     */
    private static String makeQualifiedMethodName(String name, String[] params) {
	StringBuffer sb = new StringBuffer(name);
	sb.append('=');
	for (int i = 0; i < params.length; i++) {
	    sb.append(':');
	    sb.append(params[i]);
	}
	return sb.toString();
    }

    private int getTargetDefaultEventIndex() {
	return defaultEventIndex;
    }

    private int getTargetDefaultPropertyIndex() {
	return defaultPropertyIndex;
    }

    private BeanDescriptor getTargetBeanDescriptor() {
	// Use explicit info, if available,
	if (explicitBeanInfo != null) {
	    BeanDescriptor bd = explicitBeanInfo.getBeanDescriptor();
	    if (bd != null) {
		return (bd);
	    }
	}
	// OK, fabricate a default BeanDescriptor.
	return (new BeanDescriptor(beanClass));
    }

    private boolean isEventHandler(Method m) {
	// We assume that a method is an event handler if it has a single
        // argument, whose type inherit from java.util.Event.
	Class argTypes[] = m.getParameterTypes();
	if (argTypes.length != 1) {
	    return false;
	}
	if (isSubclass(argTypes[0], java.util.EventObject.class)) {
	    return true;
	}
	return false;
    }

    /*
     * Internal method to return *public* methods within a class.
     */
    private static synchronized Method[] getPublicDeclaredMethods(Class clz) {
	// Looking up Class.getDeclaredMethods is relatively expensive,
	// so we cache the results.
	Method[] result = null;
	if (!ReflectUtil.isPackageAccessible(clz)) {
	    return new Method[0];
	}
	final Class fclz = clz;
	Reference ref = (Reference)declaredMethodCache.get(fclz);
	if (ref != null) {
	    result = (Method[])ref.get();
	    if (result != null) {
		return result;
	    }
	}

	// We have to raise privilege for getDeclaredMethods
	result = (Method[]) AccessController.doPrivileged(new PrivilegedAction() {
		public Object run() {
		    return fclz.getDeclaredMethods();
		}
	    });


	// Null out any non-public methods.
	for (int i = 0; i < result.length; i++) {
	    Method method = result[i];
	    int mods = method.getModifiers();
	    if (!Modifier.isPublic(mods)) {
	 	result[i] = null;
	    }
        }    
	// Add it to the cache.
	declaredMethodCache.put(fclz, new SoftReference(result));
	return result;
    }

    //======================================================================
    // Package private support methods.
    //======================================================================

    /**
     * Internal support for finding a target methodName with a given
     * parameter list on a given class.
     */
    private static Method internalFindMethod(Class start, String methodName,
                                                 int argCount, Class args[]) {
        // For overriden methods we need to find the most derived version.
        // So we start with the given class and walk up the superclass chain.

	Method method = null;

        for (Class cl = start; cl != null; cl = cl.getSuperclass()) {
            Method methods[] = getPublicDeclaredMethods(cl);
            for (int i = 0; i < methods.length; i++) {
                method = methods[i];
                if (method == null) {
                    continue;
                }

                // make sure method signature matches.
                Class params[] = method.getParameterTypes();
                if (method.getName().equals(methodName) && 
                    params.length == argCount) {
		    if (args != null) {
			boolean different = false;
			if (argCount > 0) {
			    for (int j = 0; j < argCount; j++) {
				if (params[j] != args[j]) {
				    different = true;
				    continue;
				}
			    }
			    if (different) {
				continue;
			    }
			}
		    }
                    return method;
                }
            }
        }
	method = null;

        // Now check any inherited interfaces.  This is necessary both when
        // the argument class is itself an interface, and when the argument
        // class is an abstract class.
        Class ifcs[] = start.getInterfaces();
        for (int i = 0 ; i < ifcs.length; i++) {
	    // Note: The original implementation had both methods calling
	    // the 3 arg method. This is preserved but perhaps it should
	    // pass the args array instead of null.
            method = internalFindMethod(ifcs[i], methodName, argCount, null);
	    if (method != null) {
		break;
	    }
        }
        return method;
    }

    /**
     * Find a target methodName on a given class.
     */
    static Method findMethod(Class cls, String methodName, int argCount) {
	return findMethod(cls, methodName, argCount, null);
    }

    /**
     * Find a target methodName with specific parameter list on a given class.
     * <p>
     * Used in the contructors of the EventSetDescriptor, 
     * PropertyDescriptor and the IndexedPropertyDescriptor.
     * <p>
     * @param cls The Class object on which to retrieve the method.
     * @param methodName Name of the method.
     * @param argCount Number of arguments for the desired method.
     * @param args Array of argument types for the method.
     * @return the method or null if not found
     */
    static Method findMethod(Class cls, String methodName, int argCount, 
                             Class args[]) {
        if (methodName == null) {
            return null;
        }
        return internalFindMethod(cls, methodName, argCount, args);
    }

    /**
     * Return true if class a is either equivalent to class b, or
     * if class a is a subclass of class b, i.e. if a either "extends"
     * or "implements" b.
     * Note tht either or both "Class" objects may represent interfaces.
     */
    static  boolean isSubclass(Class a, Class b) {
	// We rely on the fact that for any given java class or
        // primtitive type there is a unqiue Class object, so
	// we can use object equivalence in the comparisons.
	if (a == b) {
	    return true;
	}
	if (a == null || b == null) {
	    return false;
	}
	for (Class x = a; x != null; x = x.getSuperclass()) {
	    if (x == b) {	
		return true;
	    }
	    if (b.isInterface()) {
		Class interfaces[] = x.getInterfaces();
		for (int i = 0; i < interfaces.length; i++) {
		    if (isSubclass(interfaces[i], b)) {
			return true;
		    }
		}
	    }
	}
	return false;
    }

    /**
     * Return true iff the given method throws the given exception.
     */
    private boolean throwsException(Method method, Class exception) {
	Class exs[] = method.getExceptionTypes();
	for (int i = 0; i < exs.length; i++) {
	    if (exs[i] == exception) {
		return true;
	    }
	}
	return false;
    }


    /**
     * Try to create an instance of a named class.
     * First try the classloader of "sibling", then try the system
     * classloader then the class loader of the current Thread.
     */
    static Object instantiate(Class sibling, String className)
		 throws InstantiationException, IllegalAccessException,
						ClassNotFoundException {
	// First check with sibling's classloader (if any). 
	ClassLoader cl = sibling.getClassLoader();
	if (cl != null) {
	    try {
	        Class cls = cl.loadClass(className);
		return cls.newInstance();
	    } catch (Exception ex) {
	        // Just drop through and try the system classloader.
	    }
        }

	// Now try the system classloader.
	try {
	    cl = ClassLoader.getSystemClassLoader();
	    if (cl != null) {
	        Class cls = cl.loadClass(className);
		return cls.newInstance();
	    }
        } catch (Exception ex) {
	    // We're not allowed to access the system class loader or
	    // the class creation failed.
	    // Drop through.
	}

	// Use the classloader from the current Thread.
	cl = Thread.currentThread().getContextClassLoader();
	Class cls = cl.loadClass(className);
	return cls.newInstance();
    }

} // end class Introspector

//===========================================================================

/**
 * Package private implementation support class for Introspector's
 * internal use.
 * <p>
 * Mostly this is used as a placeholder for the descriptors.
 */

class GenericBeanInfo extends SimpleBeanInfo {

    private BeanDescriptor beanDescriptor;
    private EventSetDescriptor[] events;
    private int defaultEvent;
    private PropertyDescriptor[] properties;
    private int defaultProperty;
    private MethodDescriptor[] methods;
    private BeanInfo targetBeanInfo;

    public GenericBeanInfo(BeanDescriptor beanDescriptor,
		EventSetDescriptor[] events, int defaultEvent,
		PropertyDescriptor[] properties, int defaultProperty,
		MethodDescriptor[] methods, BeanInfo targetBeanInfo) {
	this.beanDescriptor = beanDescriptor;
	this.events = events;
	this.defaultEvent = defaultEvent;
	this.properties = properties;
	this.defaultProperty = defaultProperty;
	this.methods = methods;
	this.targetBeanInfo = targetBeanInfo;
    }

    /**
     * Package-private dup constructor
     * This must isolate the new object from any changes to the old object.
     */
    GenericBeanInfo(GenericBeanInfo old) {

	beanDescriptor = new BeanDescriptor(old.beanDescriptor);
	if (old.events != null) {
	    int len = old.events.length;
	    events = new EventSetDescriptor[len];
	    for (int i = 0; i < len; i++) {
		events[i] = new EventSetDescriptor(old.events[i]);
	    }
	}
	defaultEvent = old.defaultEvent;
	if (old.properties != null) {
	    int len = old.properties.length;
	    properties = new PropertyDescriptor[len];
	    for (int i = 0; i < len; i++) {
		PropertyDescriptor oldp = old.properties[i];
		if (oldp instanceof IndexedPropertyDescriptor) {
		    properties[i] = new IndexedPropertyDescriptor(
					(IndexedPropertyDescriptor) oldp);
		} else {
		    properties[i] = new PropertyDescriptor(oldp);
		}
	    }
	}
	defaultProperty = old.defaultProperty;
	if (old.methods != null) {
	    int len = old.methods.length;
	    methods = new MethodDescriptor[len];
	    for (int i = 0; i < len; i++) {
		methods[i] = new MethodDescriptor(old.methods[i]);
	    }
	}
	targetBeanInfo = old.targetBeanInfo;
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
	return properties;
    }

    public int getDefaultPropertyIndex() {
	return defaultProperty;
    }

    public EventSetDescriptor[] getEventSetDescriptors() {
	return events;
    }

    public int getDefaultEventIndex() {
	return defaultEvent;
    }

    public MethodDescriptor[] getMethodDescriptors() {
	return methods;
    }

    public BeanDescriptor getBeanDescriptor() {
	return beanDescriptor;
    }

    public java.awt.Image getIcon(int iconKind) {
	if (targetBeanInfo != null) {
	    return targetBeanInfo.getIcon(iconKind);
	}
	return super.getIcon(iconKind);
    }
}