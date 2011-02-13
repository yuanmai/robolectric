package android.preference;

import java.lang.reflect.InvocationTargetException;

import javassist.util.proxy.ProxyFactory;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.util.AttributeSet;

import com.xtremelabs.robolectric.Robolectric;

public class Test {

	public static void main(String argss[]) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException,
			NoSuchMethodException {

		BluetoothAdapter bluetoothAdapter = Robolectric.newInstanceOf(BluetoothAdapter.class);

		// TestAttributeSet<Preference> attributeSet = new TestAttributeSet<Preference>(attributes, resourceExtractor, attrResourceLoader, clazz);
		ProxyFactory pf = new ProxyFactory();
		pf.setSuperclass(PreferenceScreen.class);
		Class[] array = new Class[] { Context.class, AttributeSet.class };
		Object[] args = new Object[] { null, null };

		PreferenceScreen ps = (PreferenceScreen) pf.create(array, args);
	}
}
