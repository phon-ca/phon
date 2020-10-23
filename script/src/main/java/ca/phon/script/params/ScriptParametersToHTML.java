package ca.phon.script.params;

import java.util.*;

import ca.phon.visitor.*;
import ca.phon.visitor.annotation.*;

/**
 * Visitor for building HTML strings for script parameters.
 *
 */
public class ScriptParametersToHTML extends VisitorAdapter<ScriptParam> {

	private StringBuffer buffer;

	private String currentCategory = "General";

	private boolean printCategoryHeader = true;
	
	private boolean printOnlyChanged = false;
	
	/**
	 * List of paramIds/categories to always include
	 * categories are prefixed with a '@' symbol
	 */
	private List<String> includes = new ArrayList<>();

	/**
	 * List of paramIds/categories to always exclude
	 * categories are prefixed with a '@' symbol
	 */
	private List<String> excludes = new ArrayList<>();

	public ScriptParametersToHTML() {
		super();
		buffer = new StringBuffer();
	}

	@Override
	public void fallbackVisit(ScriptParam obj) {
	}

	private void printCategoryHeader() {
		if(printCategoryHeader) {
			buffer.append("\n\n#h3(\"").append(currentCategory).append("\")").append('\n');
			printCategoryHeader = false;
		}
	}

	private void printKey(String key) {
		// remove ':' from end of key as it will be added after
		if(key.endsWith(":")) {
			key = key.substring(0, key.length()-1);
		}
		if(key.trim().length() > 0) {
			buffer.append(key).append(": ");
		}
	}

	private void printValue(Object value) {
		if(value == null || value.toString().length() == 0) return;
		if(value.toString().contains("\n")) {
			buffer.append("\n```\n").append(value).append("\n```\n");
		} else {
			buffer.append("```").append(value).append("```");
		}
	}

	public boolean isPrintOnlyChanged() {
		return printOnlyChanged;
	}

	public void setPrintOnlyChanged(boolean printOnlyChanged) {
		this.printOnlyChanged = printOnlyChanged;
	}

	public List<String> getIncludes() {
		return includes;
	}

	public void setIncludes(List<String> includes) {
		this.includes = includes;
	}

	public List<String> getExcludes() {
		return excludes;
	}

	public void setExcludes(List<String> excludes) {
		this.excludes = excludes;
	}

	private boolean checkIncludeParam(ScriptParam param) {
		if(isPrintOnlyChanged()) {
			boolean isForceInclude =
					getIncludes().contains("@" + currentCategory) || getIncludes().contains(param.getParamId());
			boolean isExcluded =
					getExcludes().contains("@" + currentCategory) || getExcludes().contains(param.getParamId());

			if(isForceInclude) {
				return !isExcluded;
			} else {
				return !isExcluded && param.hasChanged();
			}
		} else {
			if(getExcludes().contains("@" + currentCategory) ||
					getExcludes().contains(param.getParamId()))
				return false;
			else
				return true;
		}
	}

	@Visits
	public void visitSeparator(SeparatorScriptParam param) {
		currentCategory = param.getParamDesc();
		printCategoryHeader = true;
	}

	@Visits
	public void visitBooleanParam(BooleanScriptParam param) {
		if(checkIncludeParam(param)) {
			printCategoryHeader();

			String name =
					(param.getParamDesc().trim().length() > 0
							? param.getParamDesc()
							: param.getLabelText());
			buffer.append("\n * ");
			printKey(name);
			if((Boolean)param.getValue(param.getParamId())) {
				printValue("yes");
			} else {
				printValue("no");
			}
		}
	}

	@Visits
	public void visitMultiBoolParam(MultiboolScriptParam param) {
		if(checkIncludeParam(param)) {
			printCategoryHeader();

			if(param.getParamDesc().trim().length() > 0) {
				buffer.append("\n * ");
				printKey(param.getParamDesc());
			}
			for(int i = 0; i < param.getNumberOfOptions(); i++) {
				buffer.append("\n   * ");
				printKey(param.getOptionText(i));

				if((Boolean)param.getValue(param.getOptionId(i))) {
					printValue("yes");
				} else {
					printValue("no");
				}
			}
		}
	}

	@Visits
	public void visitStringParam(StringScriptParam param) {
		if(checkIncludeParam(param)) {
			printCategoryHeader();

			buffer.append("\n * ");
			printKey(param.getParamDesc());
			printValue(param.getValue(param.getParamId()));
		}
	}

	@Visits
	public void visitEnumParam(EnumScriptParam param) {
		if(checkIncludeParam(param)) {
			printCategoryHeader();

			buffer.append("\n * ");
			printKey(param.getParamDesc());
			printValue(((EnumScriptParam.ReturnValue)param.getValue(param.getParamId())));
		}
	}
	
	public String getHTML() {
		return buffer.toString();
	}
	
}
