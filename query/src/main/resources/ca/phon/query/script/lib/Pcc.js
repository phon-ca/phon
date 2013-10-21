
/*
 * Calculates PCC/PVC
 */
 
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
     * @return percent correct as a string with format
     *  'numCorrect/numAttempted;numDeleted;numEpenthesized'
     */
    calc_pc_aligned: function(record, targetGroup, actualGroup, features, ignoreDiacritics) {
    	var numTarget = 0;
    	var numDeleted = 0;
    	var numActual = 0;
    	var numEpenthesized = 0;
    	var numCorrect = 0;
    
        var featureSet = FeatureSet.fromArray(features.split(","));
    
    	// check target side for numTarget, numDeleted and numCorrect
    	for(pIdx = 0; pIdx < targetGroup.numberOfPhones; pIdx++) {
    		var phone = targetGroup.getPhone(pIdx);
    
    		if(phone.featureSet.intersects(featureSet)) {
    			numTarget++;
    
    			// check aligned phone
    			var alignedData = record.getAlignmentData(phone);
    			if(alignedData != null) {
    				var actualData = alignedData[1];
    				var actualPhone = (actualData.length > 0 ? actualData[0] : null);
    				if(actualPhone != null) {
    					var targetPhoneString = 
    						(ignoreDiacritics ? StringUtils.stripDiacritics(phone.toString()) : phone.toString());
    					var actualPhoneString =
    						(ignoreDiacritics ? StringUtils.stripDiacritics(actualPhone.toString()) : actualPhone.toString());
    
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
    	for(pIdx = 0; pIdx < actualGroup.numberOfPhones; pIdx++) {
    		var phone = actualGroup.getPhone(pIdx);
    
    		if(phone.featureSet.intersects(featureSet)) {
    			numActual++;
    
    			// check aligned phone
    			var alignedData = record.getAlignmentData(phone);
    			if(alignedData != null) {
    				var targetData = alignedData[1];
    				var targetPhone = (targetData.length > 0 ? targetData[0] : null);
    				java.lang.System.out.println(targetPhone + "");
    				if(targetPhone == null) {
    					numEpenthesized++;
    				}
    			} else {
    				numEpenthesized++;
    			}
    		}
    	}
    
    	// format PCC string
    	// (numCorrect)/(numTarget-numDeleted);numDeleted;numEpenthesized
    	var retVal = 
    		numCorrect + "/" + (numTarget-numDeleted) + ";" + numDeleted + ";" + numEpenthesized;
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
     * @return PCC (standard) in the format x/y
     */
    calc_pc_standard: function(targetGroup, actualGroup, features, ignoreDiacritics) {
        var numTarget = 0;
        var numActual = 0;
        var targetVals = new Array();
    	var numCorrect = 0;
    	var numEpenthesized = 0;
    	var numProduced = 0;
    
        var featureSet = FeatureSet.fromArray(features.split(","));
    
    	// check target side for numTarget, numDeleted and numCorrect
    	for(pIdx = 0; pIdx < targetGroup.numberOfPhones; pIdx++) {
    		var phone = targetGroup.getPhone(pIdx);
    
    		if(phone.featureSet.intersects(featureSet)) {
    			numTarget++;
                var targetPhoneString = 
    			    (ignoreDiacritics ? StringUtils.stripDiacritics(phone.toString()) : phone.toString());
    		    targetVals[targetPhoneString] = 
    		        ( targetVals[targetPhoneString] ? targetVals[targetPhoneString] + 1 : 1 );
    		}
    	}
    
    	// check actual side for numActual, numEpenthesized
    	// check target side for numTarget, numDeleted and numCorrect
    	for(pIdx = 0; pIdx < actualGroup.numberOfPhones; pIdx++) {
    		var phone = actualGroup.getPhone(pIdx);
   
    		if(phone.featureSet.intersects(featureSet)) {
    		    numActual++;
                var actualPhoneString = 
    			    (ignoreDiacritics ? StringUtils.stripDiacritics(phone.toString()) : phone.toString());
    			    
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
    	// (numCorrect)/(numTarget-numDeleted);numDeleted;numEpenthesized
    	var retVal = 
    		numCorrect + "/" + (numTarget+numEpenthesized);
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
    
    this.setup_pcc_aligned_metadata = function(record, targetIpa, actualIpa, metadata) {
        if(this.includePcc) {
            var pccAligned = Pcc.calc_pc_aligned(record, targetIpa, actualIpa, "Consonant", this.ignoreDiacritics);
            metadata.put("APCC", pccAligned);
        }
        if(this.includePvc) {
            var pvcAligned = Pcc.calc_pc_aligned(record, targetIpa, actualIpa, "Vowel", this.ignoreDiacritics);
            metadata.put("APVC", pvcAligned);
        }
    };
    
    this.setup_pcc_standard_metadata = function(targetIpa, actualIpa, metadata) {
        if(this.includePcc) {
            var pccStandard = Pcc.calc_pc_standard(targetIpa, actualIpa, "Consonant", this.ignoreDiacritics);
            metadata.put("PCC", pccStandard);
        }
        if(this.includePvc) {
            var pvcStandard = Pcc.calc_pc_standard(targetIpa, actualIpa, "Vowel", this.ignoreDiacritics);
            metadata.put("PVC", pvcStandard);
        }
    };
    
};
