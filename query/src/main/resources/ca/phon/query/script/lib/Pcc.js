
/*
 * Calculates PCC/PVC
 */
importPackage(Packages.ca.phon.ipa.features)

exports.Pcc = {
	
	/**
     * Perform PCC (aligned) calculation for an aligned pair of
     * IPA values
     *
     * @param record
     * @param targetGroup
     * @param actualGroup
     * @param features - comma separated list of features
     * @param ignoreDiacritics
     *
     * @return {
    		target: numTarget,
    		correct: numCorrect,
    		deleted: numDeleted,
    		epen: numEpenthesized
    	};
     */
    calc_pc_aligned: function(group, features, ignoreDiacritics) {
    	var numTarget = 0;
    	var numDeleted = 0;
    	var numActual = 0;
    	var numEpenthesized = 0;
    	var numCorrect = 0;
    	
    	var targetGroup = (group.getIPATarget() == null ? new IPATranscript() : group.getIPATarget());
        var actualGroup = (group.getIPAActual() == null ? new IPATranscript() : group.getIPAActual());
        var alignment = group.getPhoneAlignment();
    
        var featureSet = FeatureSet.fromArray(features.split(","));
    
    	// check target side for numTarget, numDeleted and numCorrect
    	for(pIdx = 0; pIdx < targetGroup.length(); pIdx++) {
    		var phone = targetGroup.elementAt(pIdx);
    
    		if(phone.featureSet.intersects(featureSet)) {
    			numTarget++;
    
    			// check aligned phone
    			var alignedData = alignment["getAligned(java.lang.Iterable)"]([phone]);
    			if(alignedData != null) {
    				var actualPhone = alignedData[0];
    				if(actualPhone != null) {
    					var targetPhoneString = 
    						(ignoreDiacritics ? (new IPATranscript([phone])).removePunctuation().stripDiacritics().toString() : phone.toString());
    					var actualPhoneString =
    						(ignoreDiacritics ? (new IPATranscript([actualPhone])).removePunctuation().stripDiacritics().toString() : actualPhone.toString());
    
    					if( targetPhoneString == actualPhoneString ) {
    						numCorrect++;
    					}
    				} else {
    					numDeleted++;
    				}
    			} else {
    				numDeleted++;
    			}
    		}
    	}
    
    	// check actual side for numActual, numEpenthesized
    	// check target side for numTarget, numDeleted and numCorrect
    	for(pIdx = 0; pIdx < actualGroup.length(); pIdx++) {
    		var phone = actualGroup.elementAt(pIdx);
    
    		if(phone.featureSet.intersects(featureSet)) {
    			numActual++;
    
    			// check aligned phone
    			var alignedData = alignment["getAligned(java.lang.Iterable)"]([phone]);
    			if(alignedData != null) {
    				var targetPhone = alignedData[0];
    				if(targetPhone == null) {
    					numEpenthesized++;
    				}
    			} else {
    				numEpenthesized++;
    			}
    		}
    	}
    
    	var retVal = {
    		target: numTarget,
    		actual: numActual,
    		correct: numCorrect,
    		deleted: numDeleted,
    		epen: numEpenthesized
    	};
    	return retVal;
    },
    
    /**
     * Calculates PCC (standard) for a pair of ipa transcriptions.
     * In this version, direct phone alignment is not considered.
     *
     * @param targetIpa
     * @param acutalIpa
     * @param features
     * @param ignoreDiacritics
     *
     * @return {
     *     target: number of elements in target,
     *     correct: number of elements correct,
     *     epen: number of epenthesis
     * }
     */
    calc_pc_standard: function(group, features, ignoreDiacritics) {
        var numTarget = 0;
        var numActual = 0;
        var targetVals = new Array();
    	var numCorrect = 0;
    	var numEpenthesized = 0;
    	var numProduced = 0;
    	
    	var targetGroup = (group.getIPATarget() == null ? new IPATranscript() : group.getIPATarget());
    	var actualGroup = (group.getIPAActual() == null ? new IPATranscript() : group.getIPAActual());
        
        var alignment = group.getPhoneAlignment();
    
        var featureSet = FeatureSet.fromArray(features.split(","));
    
    	// check target side for numTarget, numDeleted and numCorrect
    	for(pIdx = 0; pIdx < targetGroup.length(); pIdx++) {
    		var phone = targetGroup.elementAt(pIdx);
    
    		if(phone.featureSet.intersects(featureSet)) {
    			numTarget++;
                var targetPhoneString = 
                	(ignoreDiacritics == true ? (new IPATranscript([phone])).removePunctuation().stripDiacritics().toString() : phone.toString());
    		    targetVals[targetPhoneString] = 
    		        ( targetVals[targetPhoneString] ? targetVals[targetPhoneString] + 1 : 1 );
    		}
    	}
    
    	// check actual side for numActual, numEpenthesized
    	// check target side for numTarget, numDeleted and numCorrect
    	for(pIdx = 0; pIdx < actualGroup.length(); pIdx++) {
    		var phone = actualGroup.elementAt(pIdx);
   
    		if(phone.featureSet.intersects(featureSet)) {
    		    numActual++;
                var actualPhoneString = 
                	(ignoreDiacritics == true ? (new IPATranscript([phone])).removePunctuation().stripDiacritics().toString() : phone.toString());
    			    
    			var amountInTarget = targetVals[actualPhoneString];
    			if(amountInTarget != null && amountInTarget > 0) {
    			    numCorrect++;
    			    targetVals[actualPhoneString] = --amountInTarget;
    			}
    		}
    	}
    	if(numActual > numTarget)
    	    numEpenthesized = numActual - numTarget;
    
    	// format PCC string
    	var retVal = {
    		target: numTarget,
    		actual: numActual,
    		correct: numCorrect,
    		epen: numEpenthesized
    	};
    	return retVal;
    }

};

exports.PccOptions = function(id, aligned) {
    
    var includePccParamInfo = {
        "id": id+(".includePcc"),
        "title": "",
        "desc": "Include percent consonants correct (" + (aligned ? "A" : "") + "PCC)",
        "def": false
    };
    var includePccParam;
    this.includePcc;
    
    var includePvcParamInfo = {
        "id": id+(".includePvc"),
        "title": "",
        "desc": "Include percent vowels correct (" + (aligned ? "A" : "") + "PVC)",
        "def": false
    };
    var includePvcParam;
    this.includePvc;
    
    var ignoreDiacriticsParamInfo = {
        "id": id+(".ignoreDiacritics"),
        "title": "",
        "desc": "Ignore diacritics",
        "def": true
    };
    var ignoreDiacriticsParam;
    this.ignoreDicacritic;
    
    this.param_setup = function(params) {
        includePccParam = new BooleanScriptParam(
            includePccParamInfo.id,
            includePccParamInfo.desc,
            includePccParamInfo.title,
            includePccParamInfo.def);
            
        includePvcParam = new BooleanScriptParam(
            includePvcParamInfo.id,
            includePvcParamInfo.desc,
            includePvcParamInfo.title,
            includePvcParamInfo.def);
        
        ignoreDiacriticsParam = new BooleanScriptParam(
            ignoreDiacriticsParamInfo.id,
            ignoreDiacriticsParamInfo.desc,
            ignoreDiacriticsParamInfo.title,
            ignoreDiacriticsParamInfo.def);
            
        params.add(includePccParam);
        params.add(includePvcParam);
        params.add(ignoreDiacriticsParam);
    };
    
    this.setup_pcc_aligned_metadata = function(group, metadata) {
    	var nf = java.text.NumberFormat.getNumberInstance();
    	nf.setMaximumFractionDigits(6);
        if(this.includePcc == true) {
            var pccAligned = Pcc.calc_pc_aligned(group, "Consonant", this.ignoreDiacritics);
            metadata.put("APCC # Target", pccAligned.target + "");
            metadata.put("APCC # Attempted", pccAligned.actual + "");
            metadata.put("APCC # Correct", pccAligned.correct + "");
            metadata.put("APCC # Deleted", pccAligned.deleted + "");
            metadata.put("APCC # Epenthesized", pccAligned.epen + "");
            var pCorrect = (pccAligned.target > 0 ? pccAligned.correct/pccAligned.target : 0) * 100;
            metadata.put("APCC % Correct", nf.format(pCorrect));
            var pDeleted = (pccAligned.target > 0 ? pccAligned.deleted/pccAligned.target : 0) * 100;
            metadata.put("APCC % Deleted", nf.format(pDeleted));
            var pEpen = (pccAligned.target > 0 ? pccAligned.epen/pccAligned.target : 0) * 100;
            metadata.put("APCC % Epenthesized", nf.format(pEpen));
        }
        if(this.includePvc == true) {
            var pvcAligned = Pcc.calc_pc_aligned(group, "Vowel", this.ignoreDiacritics);
            metadata.put("APVC # Target", pvcAligned.target + "");
            metadata.put("APVC # Attempted", pvcAligned.actual + "");
            metadata.put("APVC # Correct", pvcAligned.correct + "");
            metadata.put("APVC # Deleted", pvcAligned.deleted + "");
            metadata.put("APVC # Epenthesized", pvcAligned.epen + "");
            var pCorrect = (pvcAligned.target > 0 ? pvcAligned.correct/pvcAligned.target : 0) * 100;
            metadata.put("APVC % Correct", nf.format(pCorrect));
            var pDeleted = (pvcAligned.target > 0 ? pvcAligned.deleted/pvcAligned.target : 0) * 100;
            metadata.put("APVC % Deleted", nf.format(pDeleted));
            var pEpen = (pvcAligned.target > 0 ? pvcAligned.epen/pvcAligned.target : 0) * 100;
            metadata.put("APVC % Epenthesized", nf.format(pEpen));
        }
    };
    
    this.setup_pcc_standard_metadata = function(group, metadata) {
    	var nf = java.text.NumberFormat.getNumberInstance();
    	nf.setMaximumFractionDigits(6);
        if(this.includePcc == true) {
            var pccStandard = Pcc.calc_pc_standard(group, "Consonant", this.ignoreDiacritics);
            metadata.put("PCC # Target", pccStandard.target + "");
            metadata.put("PCC # Attempted", pccStandard.actual + "");
            metadata.put("PCC # Correct", pccStandard.correct + "");
            metadata.put("PCC # Epenthesized", pccStandard.epen + "");
            var pCorrect = (pccStandard.target > 0 ? pccStandard.correct/pccStandard.target : 0) * 100;
            metadata.put("PCC % Correct", nf.format(pCorrect));
            var pEpen = (pccStandard.target > 0 ? pccStandard.epen/pccStandard.target : 0) * 100;
            metadata.put("PCC % Epenthesized", nf.format(pEpen));
        }
        if(this.includePvc == true) {
            var pvcStandard = Pcc.calc_pc_standard(group, "Vowel", this.ignoreDiacritics);
            metadata.put("PVC # Target", pvcStandard.target + "");
            metadata.put("PVC # Attempted", pvcStandard.actual + "");
            metadata.put("PVC # Correct", pvcStandard.correct + "");
            metadata.put("PVC # Epenthesized", pvcStandard.epen + "");
            var pCorrect = (pvcStandard.target > 0 ? pvcStandard.correct/pvcStandard.target : 0) * 100;
            metadata.put("PVC % Correct", nf.format(pCorrect));
            var pEpen = (pvcStandard.target > 0 ? pvcStandard.epen/pvcStandard.target : 0) * 100;
            metadata.put("PVC % Epenthesized", nf.format(pEpen));
        }
    };
    
};
