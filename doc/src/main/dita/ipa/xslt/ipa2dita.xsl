<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
    xmlns:ipa="http://phon.ling.mun.ca/ns/ipa" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:fs="http://phon.ling.mun.ca/ns/features"
    xmlns:hex="http://phon.ling.mun.ca/xslt/functions"
    exclude-result-prefixes="#all">
    
    <xsl:function name="hex:dec">
        <xsl:param name="str"/>
        <xsl:if test="$str != ''">
            <xsl:variable name="len" select="string-length($str)"/>
            <xsl:variable name="len" select="string-length($str)"/>
            <xsl:value-of
                select="if ( $len &lt; 2 ) then string-length(substring-before('0 1 2 3 4 5 6 7 8 9 AaBbCcDdEeFf',$str)) idiv 2 else hex:dec(substring($str,1,$len - 1))*16+hex:dec(substring($str,$len))"
            />
        </xsl:if>
    </xsl:function>
    
    <!-- writing to an xml file -->
    <xsl:output method="xml" encoding="UTF-8"
        doctype-public="-//OASIS//DTD DITA Concept//EN"
        doctype-system="concept.dtd"
        indent="yes"/>
    
    <xsl:key name="unicodeKey" match="ipa:glyph" use="@value"/>
    
    <xsl:template match="/">
        <concept id="concept_ipa_listing">
            <title>Listing of IPA Characters</title>
            <shortdesc>Listing of IPA characters.</shortdesc>
            <conbody>
                <p>The following is a lising of all the supported IPA characters/glyphs.<table frame="all"
                    id="table_kyq_t4l_3g">
                    <title>Supported IPA Characters</title>
                    <tgroup cols="5">
                        <colspec colname="c1" colnum="1" colwidth="1*"/>
                        <colspec colname="c2" colnum="2" colwidth="1.34*"/>
                        <colspec colname="c3" colnum="3" colwidth="2.18*"/>
                        <colspec colname="c4" colnum="4" colwidth="3.16*"/>
                        <colspec colname="c5" colnum="5" colwidth="6.58*"/>
                        <thead>
                            <row>
                                <entry>Glyph</entry>
                                <entry>Unicode Value</entry>
                                <entry>Name</entry>
                                <entry>Type</entry>
                                <entry>Features</entry>
                            </row>
                        </thead>
                        <tbody>
                            <xsl:apply-templates/>
                        </tbody>
                    </tgroup>
                </table>
                </p>
            </conbody>
        </concept>
        
    </xsl:template>
    
    <xsl:template match="ipa:glyph">
        <xsl:variable name="tokenType">
            <xsl:value-of select="ipa:token"/>
        </xsl:variable>
        <xsl:variable name="unicodeHex"/>
        <row>
        <xsl:analyze-string select="@value" regex="0x0*([1-9a-fA-F][0-9a-fA-F]+)">
            <xsl:matching-substring>
                <xsl:variable name="value"><xsl:text>U</xsl:text><xsl:value-of
                    select="regex-group(1)"/></xsl:variable>
                <xsl:variable name="int_value">
                    <xsl:value-of select="hex:dec(regex-group(1))"
                    /></xsl:variable>
                <entry>
                <xsl:if
                    test="matches($tokenType, 'SUFFIX_DIACRITIC') or matches($tokenType, 'COMBINING_DIACRITIC') or matches($tokenType, 'ROLE_REVERSAL') or matches($tokenType, 'LIGATURE')">
                    <xsl:value-of select="codepoints-to-string(hex:dec('25cc'))"/>
                </xsl:if><xsl:value-of select="codepoints-to-string($int_value)"/><xsl:if
                    test="matches($tokenType, 'PREFIX_DIACRITIC')">
                    <xsl:value-of select="codepoints-to-string(hex:dec('25cc'))"/>
                </xsl:if></entry><entry><xsl:value-of select="$value"/></entry>
            </xsl:matching-substring>
        </xsl:analyze-string>
            <entry><xsl:value-of select="ipa:name"/></entry>
            <entry><xsl:value-of select="replace(lower-case(ipa:token), '_', ' ')"/></entry>
            <entry><xsl:analyze-string select="@value"
                regex="0x0*([1-9a-fA-F][0-9a-fA-F]+)">
                <xsl:matching-substring>
                    <xsl:variable name="value">
                        <xsl:text>U</xsl:text>
                        <xsl:value-of select="regex-group(1)"/>
                    </xsl:variable>
                    <xsl:value-of
                        select="document('http://phon.ling.mun.ca/hg/phon-2.0/raw-file/tip/ipa/src/main/resources/ca/phon/ipa/features/features.xml')//fs:feature_set[@unicode=$value]"
                    />
                </xsl:matching-substring>
            </xsl:analyze-string></entry>
            
            
        </row>
        
    </xsl:template>
    
</xsl:stylesheet>

