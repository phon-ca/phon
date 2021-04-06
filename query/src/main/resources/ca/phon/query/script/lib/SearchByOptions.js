
exports.SearchByOptions = function(id) {

    var filterTypeParamInfo = {
		"id": id + ".filterType",
		"title": "Expression type:",
		"desc":[ "Plain text", "Regular expression", "Phonex", "Stress pattern", "CGV pattern"],
		"def": 0
	};

    var searchByParamInfo = {
        "id" : id + ".searchBy",
        "title": "Search by",
        "desc": [ "Group", "Word" ],
        "def": 0,
        "type": "radiobutton",
        "cols": 0
    };
    var searchByParam;
    this.searchBy = {
        "index": 0,
        "toString": "Group"
    };

    this.includeSyllableOption = false;

    var thenBySyllableParamInfo = {
        "id": id + ".searchBySyllable",
        "title": "",
        "desc": "Then by syllable",
        "def": false
    };
    var thenBySyllableParam;
    this.searchBySyllable = thenBySyllableParamInfo.def;

    var includePositionalInformationParamInfo = {
        "id": id + ".includePositionalInfo",
        "title": "",
        "desc": "Include positional information (initial/medial/final)",
        "def": false
    };
    var includePositionalInformationParam;
    this.includePositionalInfo = includePositionalInformationParamInfo.def;

    this.includePositionalOption = false;

    // these params are given during param_setup and references are kept here
    this.searchByWordParam = null;
    this.searchBySyllableParam = null;

    this.param_setup = function(params, searchByWordParam, searchBySyllableParam, index) {
        this.searchByWordParam = searchByWordParam;
        this.searchBySyllableParam = searchBySyllableParam;

        if(index < 0) index = params.size();

        searchByParam = new EnumScriptParam(
            searchByParamInfo.id,
            searchByParamInfo.title,
            searchByParamInfo.def,
            searchByParamInfo.desc,
            searchByParamInfo.type,
            searchByParamInfo.cols);
        if(searchByWordParam && searchByWordParam.getValue(searchByWordParam.getParamId()) == true) {
            searchByParam.setValue(searchByParamInfo.id, new java.lang.Integer(1));
        }

        var searchByWordListener = new java.beans.PropertyChangeListener() {
            propertyChange: function (e) {
                var isSearchByWord = searchByWordParam.getValue(searchByWordParam.getParamId());
                var selection = (isSearchByWord == true ? 1 : 0);
                searchByParam.setValue(searchByParamInfo.id, new java.lang.Integer(selection));
            }
        };
        searchByWordParam.addPropertyChangeListener(searchByWordParam.getParamId(), searchByWordListener);

        var searchByParamListener = new java.beans.PropertyChangeListener() {
			propertyChange: function (e) {
		        if(searchByWordParam) {
		            searchByWordParam.setValue(searchByWordParam.getParamId(), searchByParam.getValue(searchByParam.getParamId()).index == 1);
		        }
		    }
	    };
        searchByParam.addPropertyChangeListener(searchByParamInfo.id, searchByParamListener);
        params.add(index++, searchByParam);

        if(this.includeSyllableOption == true && searchBySyllableParam) {
            thenBySyllableParam = new BooleanScriptParam(
                thenBySyllableParamInfo.id,
                thenBySyllableParamInfo.desc,
                thenBySyllableParamInfo.title,
                thenBySyllableParamInfo.def);
            params.add(index++, thenBySyllableParam);

            var searchBySyllListener = new java.beans.PropertyChangeListener() {
                propertyChange: function (e) {
                    thenBySyllableParam.setValue(thenBySyllableParamInfo.id, searchBySyllableParam.getValue(searchBySyllableParam.getParamId()));
                }
            };
            searchBySyllableParam.addPropertyChangeListener(searchBySyllableParam.getParamId(), searchBySyllListener);

            var thenBySyllableListener = new java.beans.PropertyChangeListener() {
                propertyChange: function (e) {
                    if(searchBySyllableParam) {
                        searchBySyllableParam.setValue(searchBySyllableParam.getParamId(),
                            thenBySyllableParam.getValue(thenBySyllableParamInfo.id));
                    }
                }
            };
            thenBySyllableParam.addPropertyChangeListener(thenBySyllableListener);
        }

        if(this.includePositionalOption == true) {
            includePositionalInformationParam = new BooleanScriptParam(
                includePositionalInformationParamInfo.id,
                includePositionalInformationParamInfo.desc,
                includePositionalInformationParamInfo.title,
                includePositionalInformationParamInfo.def
            );
            params.add(index++, includePositionalInformationParam);
        }
    };

};
