/**
 * Options for including cv pattern values
 * as metadata.
 */
 
exports.CvPatternOptions = function(id) {
    
    var includeParamInfo = {
        "id": id+".include",
        "title": "",
        "desc": "Include CGV pattern (CGV)",
        "def": false
    };
    var includeParam;
    this.include = includeParamInfo.def;
    
    var separateParamInfo = {
        "id": id+".separate",
        "title": "",
        "desc": "Use separate metadata fields for IPA Target (CGV-T) and IPA Actual (CGV-A)",
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
