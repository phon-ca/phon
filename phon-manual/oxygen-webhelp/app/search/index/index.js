var words = {};
Object.assign(words, index1, index2, index3);

var index = {
	/**
     * The object with indexed words.
     *
     * {"word" : "topicID*score, topicID*score"}
     */
    w : words,
    /**
     * Auto generated list of analyzer stop words that must be ignored by search.
     */
    stopWords : stopwords,

    /**
     * File info list.
     */
    fil : htmlFileInfoList,

    link2parent : linkToParent
}