package com.attask.jenkins.codereviewer;

import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;

/**
 * User: Joel Johnson
 * Date: 7/3/12
 * Time: 3:20 PM
 */
public class MirroredCodeReviewBuildWrapper extends BuildWrapper {
	private final String projectName;
	private final String descriptionPattern;

	@DataBoundConstructor
	public MirroredCodeReviewBuildWrapper(String projectName, String descriptionPattern) {
		this.projectName = projectName;
		this.descriptionPattern = descriptionPattern;
	}

	@Override
	public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
		EnvVars envVars = build.getEnvironment(listener);
		String projectName = envVars.expand(this.projectName);
		build.addAction(new MirroredCodeReviewAction(build, projectName, descriptionPattern));
		return new Environment() {
			@Override
			public boolean tearDown(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
				return true;
			}
		};
	}

	public String getProjectName() {
		return projectName;
	}

	public String getDescriptionPattern() {
		return descriptionPattern;
	}

	@Extension
	public static class DescriptorImpl extends BuildWrapperDescriptor {
		@Override
		public boolean isApplicable(AbstractProject<?, ?> item) {
			return true;
		}

		@Override
		public String getDisplayName() {
			return "Mirror code review of another job";
		}
	}
}
