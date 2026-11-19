package org.ftn.vecDSL.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.ftn.vecDSL.coverage.CoverageIndex;
import org.ftn.vecDSL.vecDSL.Script;

/** The coverage of one construct for every vendor a script targets, in the order of its 'targets' list. */
public record TargetStatus(Construct construct, List<VendorStatus> vendors) {

	public static TargetStatus of(CoverageIndex index, List<String> targets, Construct construct) {
		List<VendorStatus> vendors = new ArrayList<>();
		for (String vendor : targets) {
			vendors.add(new VendorStatus(vendor, construct == null ? null
					: index.entry(vendor, construct.type(), construct.feature()).orElse(null)));
		}
		return new TargetStatus(construct, vendors);
	}

	/** The targets of the script the object belongs to; empty when it declares none. */
	public static List<String> targets(Script script) {
		return script == null ? List.of() : List.copyOf(script.getTargets());
	}

	/** Whether a targeted vendor lacks the construct: content assist ranks such a proposal last. */
	public boolean isUnsupported() {
		return vendors.stream().anyMatch(VendorStatus::isUnsupported);
	}

	/** Short text after a proposal: the unsupported vendors with their reasons, or every vendor's label. */
	public String summary() {
		List<VendorStatus> shown = isUnsupported() ? vendors.stream().filter(VendorStatus::isUnsupported).toList()
				: vendors;
		return shown.stream().map(VendorStatus::describe).collect(Collectors.joining("; "));
	}
}
