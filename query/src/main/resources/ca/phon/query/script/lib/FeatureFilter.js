/*
 * Feature set filter
 */

exports.FeatureFilter = function(id) {
    
    var featureListParamInfo = {
        "id": id+".featureList",
        "def": "",
        "prompt": "Enter a set of features, separated by ','",
        "title": "Feature list:"
    };
    var featureListParam;
    this.featureList = "";
    
    this.setFilterTitle = function(title) {
        featureListParamInfo.title = title;
    };
    
    var setFilterInvalid = function(textField, message, loc) {
        var msg = (loc >= 0 ?
            "Error at index " + loc  +": " + message :
            message);
       
       // textField.setToolTipText(msg);
       // textField.setState("UNDEFINED");
    };
    
    var setFilterOk = function(textField) {
        //textField.setToolTipText("");
        //textField.setState("INPUT");
    };
    
    var checkFilter = function(filter) {
        var retVal = {
            "valid": true,
            "message": "",
            "loc": -1
        };
        if(filter == null || filter.length() == 0) return retVal;
        
        var features = filter.split(",");
        for(var i = 0; i < features.length; i++) {
            var feature = StringUtils.strip(features[i]);
            
            if(feature.startsWith("-")) {
                feature = feature.substring(1);
            }
            
            var validFeature = FeatureMatrix.getInstance().getFeature(feature) != null;
            retVal.valid &= validFeature;
            
            if(!retVal.valid) {
                retVal.message = "Unknown feature: " + feature;
                break;
            }
        }
        return retVal;
    };
    
    
    
    this.param_setup = function(params) {
        var featureListParam = new(
            featureListParamInfo.id,
            featureListParamInfo.title,
            featureListParamInfo.def);
//        featureListParam.getEditorComponent().setPrompt(featureListParamInfo.prompt);
    
//        var featureListListener = new java.awt.event.KeyListener() {
//            keyPressed: function(e) {},
//            
//            keyReleased: function(e) {
//                var txt = StringUtils.strip(e.getSource().getText());
//                var check = checkFilter(txt);
//                java.lang.System.out.println(check.valid + ":" + txt);
//                if(check.valid) {
//                    setFilterOk(e.getSource());
//                } else {
//                    java.lang.System.err.println(check.message);
//                    setFilterInvalid(e.getSource(), check.message, check.loc);
//                }
//            },
//            
//            keyTyped: function(e) {}
//        };
//        featureListParam.getEditorComponent().addKeyListener(featureListListener);
        
        params.add(featureListParam);
    };

    /**
     * Get the feature sets defined by this filter.
     *
     * @return an anonymous object with two properties:
     *  'required' and 'absent'
     */
    this.parse_features = function() {
        var requiredFeatures = new FeatureSet();
        var unwantedFeatures = new FeatureSet();
        
        var retVal = {
            "required": requiredFeatures,
            "absent": unwantedFeatures
        };
        
        var featureNames = this.featureList.split(",");
        for(var i = 0; i < featureNames.length; i++) {
            var feature = StringUtils.strip(featureNames[i]);
            
            if(feature.length == 0) continue;
            if(feature.startsWith("-")) {
                feature = feature.substring(1);
                unwantedFeatures.addFeature(feature);
            } else {
                requiredFeatures.addFeature(feature);
            }
        }
        
        return retVal;
    },

    /**
     * Function to test a featureset for the required
     * features.
     *
     * @param featureSet
     * @return true if the given feature set has both
     *  the required features and does not have the unwanted
     *  features, false otherwise
     */
    this.check_featurset = function(featureSet) {
        var featureSets = this.parse_features();
        var requiredFeatures = featureSets.required;
        var unwantedFeatures = featureSets.absent;
        
        var reqIntersection = FeatureSet.intersect(featureSet, requiredFeatures);
        var unwantedIntersection = FeatureSet.intersect(featureSet, unwantedFeatures);
    
        return (unwantedIntersection.size() == 0 && 
                    reqIntersection.equals(requiredFeatures));
    };
    
}