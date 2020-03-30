<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:fn="http://www.w3.org/2005/xpath-functions"
    xmlns:ipa="https://phon.ca/ns/ipa" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:fs="https://phon.ca/ns/features"
    xmlns:hex="https://phon.ca/xslt/functions"
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
        doctype-public="-//OASIS//DTD DITA Reference//EN"
        doctype-system="reference.dtd"
        indent="yes"/>
    
    <xsl:key name="unicodeKey" match="ipa:glyph" use="@value"/>
    
    <xsl:template match="/">
        <reference id="ipa_listing">
            <title>Listing of IPA Characters</title>
            <refbody>
                <section id="section_1">
                  <p>The following is a lising of all supported IPA characters along with the glyph unicode value, name, token type and feature set.<table frame="all"
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
                              <xsl:apply-templates select="ipa:ipa/ipa:char"/>
                          </tbody>
                      </tgroup>
                  </table>
                  </p>
                </section>
            </refbody>
        </reference>
        
    </xsl:template>
    
    <xsl:function name="ipa:int-to-hex" as="xs:string">
        <xsl:param name="in" as="xs:integer"/>
        <xsl:sequence
            select="if ($in eq 0)
            then '0'
            else
            concat(if ($in gt 16)
            then ipa:int-to-hex($in idiv 16)
            else '',
            substring('0123456789ABCDEF',
            ($in mod 16) + 1, 1))"/>
    </xsl:function>
    
    <xsl:template match="ipa:char">
        <xsl:variable name="tokenType">
            <xsl:value-of select="replace(lower-case(ipa:token), '_', ' ')"/>
        </xsl:variable>
        <xsl:variable name="value">
            <xsl:value-of select="@value"/>
        </xsl:variable>
        <row>
        
            <entry><xsl:if test="$tokenType != 'combining diacritic'"><xsl:value-of select="@value"/></xsl:if><xsl:if test="$tokenType = 'combining diacritic'"><xsl:value-of select="concat('&#x25cc;', @value)"/></xsl:if></entry>
            <entry><xsl:text>0x</xsl:text><xsl:value-of select="ipa:int-to-hex(fn:string-to-codepoints(@value))"/></entry>
            <entry><xsl:value-of select="ipa:name"/></entry>
            <entry><xsl:value-of select="$tokenType"/></entry>
            <entry>
                    <xsl:value-of
                        select="document('../../../target/xml/ca/phon/ipa/features/features.xml')//fs:feature_set[@char=$value]"
                    />
            </entry>
            
            
        </row>
        
    </xsl:template>
    
</xsl:stylesheet>

