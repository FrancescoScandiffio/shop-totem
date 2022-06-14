package com.github.raffaelliscandiffio.mysql;

import java.util.Collections;
import java.util.List;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.MostUsefulConfiguration;
import org.jbehave.core.junit.JUnitStories;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.reporters.StoryReporterBuilder;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.InstanceStepsFactory;

public class MySqlStoryRunner extends JUnitStories {
	public List<String> storyPaths() {
		return Collections.singletonList("stories/mysqlStory.story");
	}

	@Override
	public Configuration configuration() {
		return new MostUsefulConfiguration().useStoryReporterBuilder(
				new StoryReporterBuilder().withDefaultFormats().withFormats(Format.CONSOLE, Format.HTML));
	}

	@Override
	public InjectableStepsFactory stepsFactory() {
		return new InstanceStepsFactory(configuration(), new MySqlStorySteps());
	}
}