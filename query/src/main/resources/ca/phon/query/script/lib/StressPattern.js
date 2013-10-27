/**
 * Options for including stress pattern values
 * as metadata.
 */
 
exports.StressPatternOptions = function(id) {
    
    var includeParamInfo = {
        "id": id+".include",
        "title": "",
        "desc": "Include stress pattern (SP)",
        "def": false
    };
    var includeParam;
    this.include = includeParamInfo.def;
    
    var separateParamInfo = {
        "id": id+".separate",
        "title": "",
        "desc": "Use separate metadata fields for IPA Target (SP-T) and IPA Actual (SP-A)",
        "def": false
    };
    var separateParam;
    this.separate = separateParamInfo.def;
    
    this.param_setup = function(params) {
        includeParam = new BooleanScriptParam(
            includeParamInfo.id,
            includeParamInfo.desc,
            includeParamInfo.title,
            includeParamInfo.def);
            
        separateParam = new BooleanScriptParam(
            separateParamInfo.id,
            separateParamInfo.desc,
            separateParamInfo.title,
            separateParamInfo.def);
    
        params.add(includeParam);
        params.add(separateParam);
    };

};
