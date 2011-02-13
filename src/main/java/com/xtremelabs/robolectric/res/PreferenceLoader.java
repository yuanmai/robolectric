package com.xtremelabs.robolectric.res;

import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.util.AttributeSet;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.tester.android.util.TestAttributeSet;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PreferenceLoader extends XmlLoader {

    private Map<String, PreferenceNode> preferenceNodesByXmlName = new HashMap<String, PreferenceNode>();
    private AttrResourceLoader<Preference> attrResourceLoader;

    public PreferenceLoader(ResourceExtractor resourceExtractor, AttrResourceLoader<Preference> attrResourceLoader) {
        super(resourceExtractor);
        this.attrResourceLoader = attrResourceLoader;
    }

    @Override
    protected void processResourceXml(File xmlFile, Document document, boolean ignored) throws Exception {
        PreferenceNode topLevelNode = new PreferenceNode("top-level", new HashMap<String, String>());
        processChildren(document.getChildNodes(), topLevelNode);
        preferenceNodesByXmlName.put("xml/" + xmlFile.getName().replace(".xml", ""), topLevelNode.getChildren().get(0));
    }

    private void processChildren(NodeList childNodes, PreferenceNode parent) {
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node node = childNodes.item(i);
            processNode(node, parent);
        }
    }

    private void processNode(Node node, PreferenceNode parent) {
        String name = node.getNodeName();
        NamedNodeMap attributes = node.getAttributes();
        Map<String, String> attrMap = new HashMap<String, String>();
        if (attributes != null) {
            int length = attributes.getLength();
            for (int i = 0; i < length; i++) {
                Node attr = attributes.item(i);
                attrMap.put(attr.getNodeName(), attr.getNodeValue());
            }
        }

        if (!name.startsWith("#")) {
            PreferenceNode preferenceNode = new PreferenceNode(name, attrMap);
            if (parent != null) {
                parent.addChild(preferenceNode);
            }

            processChildren(node.getChildNodes(), preferenceNode);
        }
    }

    public Preference inflatePreference(Context context, String key) {
        return inflatePreference(context, key, null);
    }

    public Preference inflatePreference(Context context, String key, Preference parent) {
        return inflatePreference(context, key, null, parent);
    }

    public Preference inflatePreference(Context context, int resourceId) {
        return inflatePreference(context, resourceExtractor.getResourceName(resourceId), null);
    }

    public Preference inflatePreference(Context context, int resourceId, Preference parent) {
        return inflatePreference(context, resourceExtractor.getResourceName(resourceId), parent);
    }

    private Preference inflatePreference(Context context, String key, Map<String, String> attributes, Preference parent) {
        PreferenceNode preferenceNode = preferenceNodesByXmlName.get(key);
        if (preferenceNode == null) {
            throw new RuntimeException("Could not find xml " + key);
        }
        try {
            if (attributes != null) {
                for (Map.Entry<String, String> entry : attributes.entrySet()) {
                    if (!entry.getKey().equals("layout")) {
                        preferenceNode.attributes.put(entry.getKey(), entry.getValue());
                    }
                }
            }
            return preferenceNode.inflate(context, parent);
        } catch (Exception e) {
            throw new RuntimeException("error inflating " + key, e);
        }
    }

    public class PreferenceNode {
        private String name;
        private final Map<String, String> attributes;

        private List<PreferenceNode> children = new ArrayList<PreferenceNode>();

        public PreferenceNode(String name, Map<String, String> attributes) {
            this.name = name;
            this.attributes = attributes;
        }

        public List<PreferenceNode> getChildren() {
            return children;
        }

        public void addChild(PreferenceNode preferenceNode) {
            children.add(preferenceNode);
        }

        public Preference inflate(Context context, Preference parent) throws Exception {
            Preference preference = create(context, (PreferenceGroup) parent);

            for (PreferenceNode child : children) {
                child.inflate(context, preference);
            }

            return preference;
        }

        private Preference create(Context context, PreferenceGroup parent) throws Exception {
            Preference preference = constructPreference(context);
            addToParent(parent, preference);
            return preference;
        }

        private void addToParent(PreferenceGroup parent, Preference preference) {
            if (parent != null && parent != preference) {
                parent.addPreference(preference);
            }
        }

        private Preference constructPreference(Context context) throws InstantiationException, IllegalAccessException, InvocationTargetException,
                NoSuchMethodException {
            Class<? extends Preference> clazz = pickPreferenceClass();
            try {
                if (clazz.equals(PreferenceScreen.class)) {
                    PreferenceScreen preferenceScreen = Robolectric.newInstanceOf(PreferenceScreen.class);
                    Robolectric.shadowOf(preferenceScreen).setContext(context);
                    return preferenceScreen;
                }
                TestAttributeSet<Preference> attributeSet = new TestAttributeSet<Preference>(attributes, resourceExtractor, attrResourceLoader, clazz);
                return ((Constructor<? extends Preference>) clazz.getConstructor(Context.class, AttributeSet.class)).newInstance(context, attributeSet);
            } catch (NoSuchMethodException e) {
                try {
                    return ((Constructor<? extends Preference>) clazz.getConstructor(Context.class)).newInstance(context);
                } catch (NoSuchMethodException e1) {
                    return ((Constructor<? extends Preference>) clazz.getConstructor(Context.class, String.class)).newInstance(context, "");
                }
            }
        }

        private Class<? extends Preference> pickPreferenceClass() {
            Class<? extends Preference> clazz = loadClass(name);
            if (clazz == null) {
                clazz = loadClass("android.preference." + name);
            }

            if (clazz == null) {
                throw new RuntimeException("couldn't find view class " + name);
            }
            return clazz;
        }

        private Class<? extends Preference> loadClass(String className) {
            try {
                // noinspection unchecked
                return (Class<? extends Preference>) getClass().getClassLoader().loadClass(className);
            } catch (ClassNotFoundException e) {
                return null;
            }
        }
    }

}
