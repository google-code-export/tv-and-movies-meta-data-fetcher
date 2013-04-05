package org.stanwood.media.test.sources;

import org.stanwood.media.Controller;
import org.stanwood.media.extensions.ExtensionException;
import org.stanwood.media.extensions.ExtensionInfo;
import org.stanwood.media.extensions.ExtensionType;
import org.stanwood.media.extensions.ParameterType;

public class TestSourceInfo extends ExtensionInfo<TestSource> {

	public TestSourceInfo() {
		super(TestSource.class.getName(),ExtensionType.SOURCE, new ParameterType[0]);
	}

	@Override
	protected TestSource createExtension(Controller controller) throws ExtensionException {
		return new TestSource();
	}

}
