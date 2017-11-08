<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:fs="https://phon.ca/ns/features"
    xmlns:hex="http://phon.ling.mun.ca/xslt/functions"
    exclude-result-prefixes="#all">
    
    <!-- writing to an xml file -->
    <xsl:output method="xml" encoding="UTF-8"
        doctype-public="-//OASIS//DTD DITA Concept//EN"
        doctype-system="concept.dtd"
        indent="yes"/>
    
    <xsl:template match="/">
        <concept id="concept_feature_listing">
            <title>Listing of phonetic features</title>
            <shortdesc>Listing of phonetic features.</shortdesc>
            <conbody>
                <p>The following is a listing of all the supported phonetic features.<table frame="all"
                    id="table_mtk_bqm_3g">
                    <title>Features</title>
                    <tgroup cols="4">
                        <colspec colname="c1" colnum="1" colwidth="1.0*"/>
                        <colspec colname="c2" colnum="2" colwidth="1.0*"/>
                        <colspec colname="c3" colnum="3" colwidth="1.0*"/>
                        <colspec colname="c4" colnum="4" colwidth="1.0*"/>
                        <thead>
                            <row>
                                <entry>Name</entry>
                                <entry>Synonyms</entry>
                                <entry>Primary Family</entry>
                                <entry>Secondary Family</entry>
                            </row>
                        </thead>
                        <tbody>
                            <xsl:apply-templates select="fs:feature_matrix/fs:feature"/>
                        </tbody>
                    </tgroup>
                </table>
                </p>
            </conbody>
        </concept>
    </xsl:template>
    
    <xsl:template match="fs:feature">
        <row>
            <entry><xsl:value-of select="@name"/></entry>
            <entry>
            <xsl:for-each select="fs:synonym">
                <xsl:value-of select="concat(text(), ' ')"/>
            </xsl:for-each>
            </entry>
            <entry>
                <xsl:value-of select="fs:primary_family"/>
            </entry>
            <entry>
                <xsl:value-of select="fs:secondary_family"/>
            </entry>
        </row>
    </xsl:template>
    
</xsl:stylesheet>
