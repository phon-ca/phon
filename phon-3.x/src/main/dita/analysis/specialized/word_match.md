<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE topic PUBLIC "-//OASIS//DTD DITA Topic//EN" "topic.dtd">
<topic id="consonants">
    <title>Consonants</title>
    <body>
        <p>The Consonants analysis consists of a series of algorithms to determine the accuracy of
            consonants in singleton and complex syllable onsets and codas as well as in
            heterosyllabic clusters. Options also include the selection of OEHSs (onsets of
            empty-headed syllables) as well as syllable appendices, which serve to identify
            particular syllabification patterns in languages like French or Dutch (among
            others).</p>
        
    </body>
    <topic id="consonaonts_parameters">
        <title>Parameters</title>
        <body>
            <fig id="fig_qnr_jn4_nlb">
                <title>Consonants Parameters</title>
                <image placement="break" href="images/consonants_parameters.png" id="image_rnr_jn4_nlb">
                    <alt>Consonants Parameters</alt>
                </image>
            </fig>
            <p>For each participant, and for each context selected in the options illustrated in Figure
                X, Phon provides a series of reports, as follows (example uses selection as show
                above):</p>
        </body>
        <topic id="consonants-across-all-positions">
            <title>Consonants across all positions</title>
            <body>
                <ul>
                    <li><p>Global Accuracy: List and frequency counts of all of the consonants, each
                            identified by syllable position (e.g. O2 = second position of syllable
                            onset) and marked for their behaviours as Accurate, or undergoing
                            Substitutions, Deletions, or Epenthesis. Sums and percentages are reported
                            for each behaviour.</p>
                        <fig id="fig_gfx_mn4_nlb">
                            <title>Consonants across all positions</title>
                            <image placement="break" href="images/consonants_table1.png"
                                id="image_hfx_mn4_nlb">
                                <alt>Consonants across all positions</alt>
                            </image>
                        </fig></li>
                    <li><p>Syllable-initial Accuracy: as per above, for syllable-initial consonants and
                            clusters.</p></li>
                    <li><p>Syllable-final Accuracy: as per above, for syllable-initial consonants and
                            clusters.</p></li>
                </ul>
            </body>
        </topic>
        <topic id="consonants-in-singleton-onsets-and-codas">
            <title>Consonants in singleton onsets and codas</title>
            <body>
                <ul>
                    <li><p>Singletons Accuracy: List and frequency counts of all of the consonants in
                            singleton onsets and codas, each identified by syllable position (e.g. O1 =
                            syllable onset) and marked for their behaviours as Accurate, or undergoing
                            Substitutions, Deletions, or Epenthesis. Sums and percentages are reported
                            for each behaviour.</p></li>
                    <li><p>Syllable-initial Accuracy: as per above, for syllable-initial consonants and
                            clusters.</p></li>
                    <li><p>Syllable-final Accuracy: as per above, for syllable-initial consonants and
                            clusters.</p></li>
                    <li><p>Accuracy report for each consonant type, by syllable position, alongside
                            descriptive behaviours, sums and percentage:</p>
                        <image placement="break" href="images/consonants_table2.png"
                            id="image_a2j_d44_nlb"/></li>
                    <li><p>Listing of each token in context, including corpus/session, the word
                            containing the consonant (in Orthography, IPA Target, IPA Actual) and the
                            context within which the token was found. Within the Phon report preview,
                            the user can click on each individual result to visualize it within its
                            corresponding data record.</p>
                        <image placement="break" href="images/consonants_table3.png"
                            id="image_bcy_d44_nlb"/></li>
                </ul>
            </body>
        </topic>
        <topic id="consonant-clusters">
            <title>Consonant clusters</title>
            <body>
                <p>The reports for consonant clusters are formatted as above, where the behaviour of
                    each consonant is listed individually. Additional consonants or vowels produced
                    which break up target clusters (e.g. blue produced as /bilu/) are also reported,
                    with the “+” diacritic to indicate their status as epenthetic.</p>
            </body>
        </topic>
        <topic id="other_paramters">
            <title>Other parameters</title>
            <body><p><ul id="ol_qkx_cqy_mjb">
                <li><xref href="../query/phones_query.dita#aligned-phones"/></li>
                <li><xref href="../query/common_query_options.dita#group_options"/></li>
                <li><xref href="../query/common_query_options.dita#word_options"/></li>
                <li><xref href="../query/common_query_options.dita#additional_tier_data"/></li>
            </ul></p></body>
        </topic>
    </topic>
    <topic id="consonants_outline">
        <title>Report Outline</title>
        <body>
            <p>An example table of contents is displayed below. Bold level elements are section
                headers while italic items are tables. Click <xref
                    href="./examples/consonants_example.html" format="html" scope="external"
                    >here</xref> to see an example report.</p>
            <ul>
                <li>
                    <p><b>Consonants</b></p>
                    <ul id="ul_pzg_gmg_3nb">
                        <li>
                            <p><b>Parameters</b></p>
                        </li>
                        <li>
                            <p><b>Participant 1</b></p>
                            <ul id="ul_dzp_5mg_3nb">
                                <li>
                                    <p><i>Global Accuracy</i></p>
                                </li>
                                <li>
                                    <p><i>Syllable-initial Accuracy</i></p>
                                    <p><i>Syllable-final Accuracy</i></p>
                                </li>
                                <li><b>Singletons</b><ul id="ul_c4l_lmg_3nb">
                                    <li><i>Singleton Accuracy</i></li>
                                    <li><i>Syllable-initial Accuracy</i></li>
                                    <li><i>Syllable-final Accuracy</i></li>
                                    <li><b>/c1/</b><ul id="ul_gqp_wmg_3nb">
                                                <li><i>Listing</i></li>
                                            </ul></li>
                                    <li><b>/c2/</b><ul id="ul_o4y_wmg_3nb">
                                                <li><i>Listing</i></li>
                                            </ul></li>
                                    <li>… (for each singleton consonant)</li>
                                </ul></li>
                                <li><b>Clusters</b><ul id="ul_sk4_qmg_3nb">
                                    <li><i>Cluster Accuracy</i></li>
                                    <li><i>Syllable-initial Accuracy</i></li>
                                    <li><i>Syllable-final Accuracy</i></li>
                                    <li><b>/c1/</b><ul id="ul_hnh_xmg_3nb">
                                                <li><i>Listing</i></li>
                                            </ul></li>
                                    <li><b>/c2/</b><ul id="ul_zds_xmg_3nb">
                                                <li><i>Listing</i></li>
                                            </ul></li>
                                    <li>… (for each consonant cluster)</li>
                                </ul></li>
                            </ul>
                        </li>
                        <li>… (for each selected participant)</li>
                    </ul>
                </li>
            </ul>
        </body>
    </topic>
</topic>
