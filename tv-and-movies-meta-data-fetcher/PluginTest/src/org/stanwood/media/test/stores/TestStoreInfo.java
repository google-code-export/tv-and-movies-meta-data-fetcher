package org.stanwood.media.test.stores;

import org.stanwood.media.Controller;
import org.stanwood.media.extensions.ExtensionException;
import org.stanwood.media.extensions.ExtensionInfo;
import org.stanwood.media.extensions.ExtensionType;
import org.stanwood.media.extensions.ParameterType;

public class TestStoreInfo extends ExtensionInfo<TestStore> {

	public TestStoreInfo() {
		super(TestStore.class.getName(),ExtensionType.STORE, new ParameterType[0]);
	}

	@Override
	protected TestStore createExtension(Controller controller) throws ExtensionException {
		return new TestStore();
	}

}
