package com.attask.jenkins.dashboard;

import hudson.Extension;
import hudson.model.*;
import hudson.scm.ChangeLogSet;
import hudson.tasks.test.AbstractTestResultAction;
import hudson.util.RunList;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A view that shows a table of builds that are related (determined by build description and a regex).
 *
 * User: joeljohnson
 * Date: 2/9/12
 * Time: 9:56 AM
 */
public class PipelineDashboard extends View {
	public static Logger LOGGER = Logger.getLogger(PipelineDashboard.class.getSimpleName());
	public static final String ORB_SIZE = "24x24";
    public String descriptionRegex;
    public int descriptionRegexGroup;
    public int numberDisplayed;
	public String firstColumnName;
	public boolean showBuildName;
	public boolean showFailureCount;
	public boolean clickForCommitDetails;
	public boolean highlightCommitter;
	public boolean showLastSuccessfulBuild;
	public boolean autoRefresh;
	public String topEmbedded;
	public String bottomEmbedded;

	public List<JobColumn> jobColumns;

	@DataBoundConstructor
	public PipelineDashboard(String name) {
		super(name);
	}
	
	@SuppressWarnings("UnusedDeclaration")
	public static Collection<String> getAllJobs() {
		return Jenkins.getInstance().getJobNames();
	}

	@Override
	protected void submit(StaplerRequest request) throws ServletException, Descriptor.FormException, IOException {
		this.jobColumns = JobColumn.parseFromRequest(request.getParameterMap());

		this.descriptionRegex = request.getParameter("_.descriptionRegex");
		String descriptionRegexGroup = request.getParameter("_.descriptionRegexGroup");
		if(descriptionRegexGroup != null) {
			this.descriptionRegexGroup = Integer.parseInt(descriptionRegexGroup);
			if(this.descriptionRegexGroup < 0) {
				this.descriptionRegexGroup = 0;
			}
		} else {
			this.descriptionRegexGroup = 0;
		}


		String numberDisplayed = request.getParameter("_.numberDisplayed");
		if(numberDisplayed == null || numberDisplayed.isEmpty()) {
			this.numberDisplayed = 5;
		} else {
			this.numberDisplayed = Integer.parseInt(numberDisplayed);
		}

		this.firstColumnName = request.getParameter("_.firstColumnName");
		
		this.showBuildName = "on".equals(request.getParameter("_.showBuildName"));
		this.showFailureCount = "on".equals(request.getParameter("_.showFailureCount"));
		this.clickForCommitDetails = "on".equals(request.getParameter("_.clickForCommitDetails"));
		this.highlightCommitter = "on".equals(request.getParameter("_.highlightCommitter"));
		this.showLastSuccessfulBuild = "on".equals(request.getParameter("_.showLastSuccessfulBuild"));
		this.autoRefresh = "on".equals(request.getParameter("_.autoRefresh"));

		this.topEmbedded = request.getParameter("_.topEmbedded");
		this.bottomEmbedded = request.getParameter("_.bottomEmbedded");
	}

	/**
	 * Finds all the builds that matches the criteria in the settings and organizes them in rows and columns.
	 * @return The list of Rows. Each row containing information about builds whose descriptions match based on the
	 * 			regex provided. The list is sorted by build date starting with the most recent.
	 */
	@SuppressWarnings("UnusedDeclaration")
	public Table getDisplayRows() { //show hidden is from a request parameter. Hence the string rather than boolean
//		LOGGER.info("getDisplayRows starting");

		Jenkins jenkins = Jenkins.getInstance();
		Map<String, Run[]> map = findMatchingBuilds(jenkins, jobColumns, descriptionRegex, descriptionRegexGroup);
//		LOGGER.info("map size: " + map.size());

		Table result = generateRowData(jenkins.getRootUrl(), User.current(), map, this.showBuildName, this.showFailureCount);
//		LOGGER.info("result size: " + result.getRows().size());

		return result;
	}

	protected Map<String, Run[]> findMatchingBuilds(ItemGroup<TopLevelItem> jenkins, List<JobColumn> jobs, String descriptionRegex, int descriptionRegexGroup) {
		if(jenkins == null || jobs == null || descriptionRegex == null) return Collections.emptyMap();
		
		Map<String, Run[]> map = new HashMap<String, Run[]>();
		for (JobColumn jobName : jobs) {
			try {
				Job job = (Job) jenkins.getItem(jobName.getJobName());
				if(job == null) continue;
				RunList builds = job.getBuilds();
				if(builds == null) continue;
				for (Object buildObj : builds) {
					Run build = (Run)buildObj;
					String buildDescription = build.getDescription();
					if(buildDescription == null || buildDescription.trim().isEmpty()) {
						continue;
					}
					buildDescription = buildDescription.replaceAll("(\n|\r)", " "); //normalize whitespace

					if(buildDescription.matches(descriptionRegex)) {
						String key = buildDescription.replaceFirst(descriptionRegex, "$" + descriptionRegexGroup);
						if(!map.containsKey(key)) {
							map.put(key, new Run[jobs.size()]);
						}
						map.get(key)[jobs.indexOf(jobName)] = build;
					}
				}
			} catch(Throwable t) {
				LOGGER.severe("Error while generating the map: " + t.getMessage() + "\n" + join(Arrays.asList(t.getStackTrace()), "\n"));
				if(t.getCause() != null) {
					LOGGER.severe("Nested Exception: " + t.getCause().getClass().getCanonicalName() + t.getCause().getMessage() + "\n" + join(Arrays.asList(t.getStackTrace()), "\n"));
				}
			}
		}
		return map;
	}

	protected Table generateRowData(String rootUrl, User currentUser, Map<String, Run[]> map, boolean showBuildName, boolean showFailureCount) {
		if(rootUrl == null) rootUrl = "";
		if(map == null) return Table.EMPTY_TABLE;
		
		SortedSet<Row> rows = new TreeSet<Row>(new Comparator<Row>() {
			public int compare(Row row1, Row row2) {
				if(row1 == row2) return 0;
				if(row1 == null) return 1;
				if(row2 == null) return -1;
				return -row1.getDate().compareTo(row2.getDate());
			}
		});

		for (String rowName : map.keySet()) {
			try {
//				LOGGER.info("Row " + rowName);
				Run[] builds = map.get(rowName);
				List<Column> columns = new LinkedList<Column>();
				Date date = null;
				String displayName = rowName;
				boolean isCulprit = false;
				boolean hasMultiple = false;

				for(int i = 0; i < builds.length; i++) {
					Run build = builds[i];
					JobColumn columnHeader = jobColumns.get(i);

					if(build != null) {
						if(!build.getParent().getName().equals(columnHeader.getJobName())) {
							LOGGER.log(Level.SEVERE, "Out of sync! columnHeader: " +  columnHeader.getJobName() + " Build: " + build.getParent().getName());
						}
//						LOGGER.info("\t" + build.getDisplayName() + " " + build.getDescription());
						if(date == null) {
							date = build.getTime();
							if(build instanceof AbstractBuild) {
								AbstractBuild abstractBuild = (AbstractBuild) build;
								hasMultiple = !abstractBuild.getChangeSet().isEmptySet() && abstractBuild.getChangeSet().getItems().length > 1;
							}
						}

						String rowDisplayName = "";
						
						if(showBuildName && build.getDisplayName() != null) {
							rowDisplayName = build.getDisplayName();
						}

						//Only show the count if it's Successful or Unstable
						// (Successful because of the Status override plugin.)
						int failureCount = -1;
						if(showFailureCount && (build.getResult() == Result.SUCCESS || build.getResult() == Result.UNSTABLE)) {
							AbstractTestResultAction testResultAction = build.getAction(AbstractTestResultAction.class);
							if(testResultAction != null) {
								failureCount = testResultAction.getFailCount();
							}
						}


						columns.add(new Column(columnHeader, rowDisplayName, failureCount, build.getUrl(), rootUrl + "/static/832a5f9d/images/" + ORB_SIZE + "/" + build.getBuildStatusUrl()));

						//noinspection StringEquality
						if(displayName == rowName && build.getDescription() != null && !build.getDescription().trim().isEmpty()) { // I really do want to do reference equals and not value equals.
							displayName = build.getDescription();
							isCulprit = getUserIsCommitter(currentUser, build);
						}
					} else {
//						LOGGER.info("\tAdded empty column");
						columns.add(Column.getEmpty(columnHeader));
					}
				}
				if(date == null) date = new Date();

				rows.add(new Row(date, rowName, displayName, columns, isCulprit, hasMultiple));
			} catch (Throwable t) {
				LOGGER.severe("Error while generating the list: " + t.getClass().getCanonicalName() + t.getMessage() + "\n" + join(Arrays.asList(t.getStackTrace()), "\n"));
				if(t.getCause() != null) {
					LOGGER.severe("Nested Exception: " + t.getCause().getClass().getCanonicalName() + t.getCause().getMessage() + "\n" + join(Arrays.asList(t.getStackTrace()), "\n"));
				}
			}
		}
		List<Row> result = new LinkedList<Row>();

		Row lastSuccessfulRow = null;

		int i = 0;
		for (Row row : rows) {
			if(lastSuccessfulRow == null && row.isPassed(jobColumns)) {
				lastSuccessfulRow = row;
			}

			if(i < numberDisplayed && i < rows.size()) {
				//Only add as many as defined. We might be iterating just to look for the last success
				result.add(row);
			} else {
				//keep looking for the last successful row even if it's not shown otherwise
				if(lastSuccessfulRow != null) {
					break;
				}
			}
			i++;
		}
		return new Table(result, lastSuccessfulRow);
	}

	private boolean getUserIsCommitter(User currentUser, Run build) {
		if(currentUser == null || build == null) return false;

		String description = build.getDescription();
		if(description.contains(currentUser.getFullName()) || description.contains(currentUser.getId())) {
			return true;
		}

		//noinspection unchecked
		if(build instanceof AbstractBuild) {
			for (Object changeObj : ((AbstractBuild)build).getChangeSet()) {
				ChangeLogSet.Entry change = (ChangeLogSet.Entry)changeObj;
				User culprit = change.getAuthor();
				if((culprit.getId() != null && culprit.getId().equals(currentUser.getId())) || (culprit.getFullName() != null && culprit.getFullName().equals(currentUser.getFullName()))) {
					return true;
				}
			}
		}
		
		return false;
	}

	/**
	 * Generates a flat string
	 * @param collection The collection to combine
	 * @param separator The string to separate each element with
	 * @return The toString of each element of the given collection, separated by the given separator.
	 */
	private String join(Collection<?> collection, String separator) {
		if(collection == null) return "";
		if(separator == null) separator = "";

		StringBuilder sb = new StringBuilder();
		for (Object s : collection) {
			sb.append(s).append(separator);
		}
		if(sb.length() > 0) {
			return sb.substring(0, sb.length() - separator.length());
		}
		return "";
	}


	@Override
	public Collection<TopLevelItem> getItems() {
		return Collections.emptyList();
	}

	@Override
	public boolean contains(TopLevelItem item) {
		return false;
	}

	@Override
	public void onJobRenamed(Item item, String oldName, String newName) {
		boolean changed = false;
		for (JobColumn jobColumn : jobColumns) {
			if(oldName.equals(jobColumn.getJobName())) {
				jobColumn.setJobName(newName);
				changed = true;
			}
		}
		if(changed) {
			try {
				owner.save();
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, "Error saving after job rename", e);
			}
		}
	}

	@Override
	public synchronized Item doCreateItem(StaplerRequest request, StaplerResponse response) throws IOException, ServletException {
		Item item = Jenkins.getInstance().doCreateItem(request, response);
		if (item != null) {
			jobColumns.add(new JobColumn(item.getName(), null, false, true));
			owner.save();
		}
		return item;
	}

	@Override
	public String toString() {
		return super.toString() + " { " +
				"description: " + this.description + ", " +
				"descriptionRegex: " + this.descriptionRegex + ", " +
				"firstColumnName: " + this.firstColumnName + ", " +
				"numberDisplayed: " + this.numberDisplayed + ", " +
				"jobs: [" + this.join(this.jobColumns, ", ") + "]" +
		"}";
	}

	@Extension
	public static final class DescriptorImpl extends ViewDescriptor {
		@Override
		public String getDisplayName() {
			return "Pipeline Dashboard";
		}
	}
}

