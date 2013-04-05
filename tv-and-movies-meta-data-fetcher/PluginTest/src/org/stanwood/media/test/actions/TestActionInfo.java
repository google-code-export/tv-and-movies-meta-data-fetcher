package org.stanwood.media.test.actions;

import org.stanwood.media.Controller;
import org.stanwood.media.extensions.ExtensionException;
import org.stanwood.media.extensions.ExtensionInfo;
import org.stanwood.media.extensions.ExtensionType;
import org.stanwood.media.extensions.ParameterType;

public class TestActionInfo extends ExtensionInfo<TestAction> {

	public TestActionInfo() {
		super(TestAction.class.getName(),ExtensionType.ACTION, new ParameterType[0]);
	}

	@Override
	protected TestAction createExtension(Controller controller) throws ExtensionException {
		return new TestAction();
	}

}
