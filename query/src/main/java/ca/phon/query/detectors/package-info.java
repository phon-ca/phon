/**
 * <link rel='stylesheet' type='text/css' href='../../../../../resources/ca/phon/query/detectors/detectors.css'/>
 * 
 * <p>This package provides detectors for Harmony and Metathesis in aligned 
 * IPA Transcriptions.</p>
 * 
 * <div id='conventions'><h2>Conventions</h2>
 * <p>
 * <ul>
 * <li>The set of all IPA characters is denoted by &#x2119;.  Likewise the mutually exclusive sets of all consonants and all vowels are represented by &#x2102; and &#x1d54d; respectively.</li>
 * <li>T is the target IPA transcription. T = [t<sub><i>1</i></sub>, t<sub><i>2</i></sub>, &hellip; , t<sub><i>n</i></sub>], t<sub><i>i</i></sub> &#x2208; &#x2119;</li>
 * <li>A is the actual IPA transcription. A = [a<sub><i>1</i></sub>, a<sub><i>2</i></sub>, &hellip; , a<sub><i>m</i></sub>], a<sub><i>i</i></sub> &#x2208; &#x2119;</li>
 * <li>M is a 2 X <i>k</i> matrix containing pair-wise alignment of T and A.  <i>k</i> is the pre-calculated length of the alignment.  Elements from
 * T are contained in the first row while elements from A are contained in the second row. There may be indels contained in the alignment (i.e., 
 * deletions or epenthesis in A) which are indicated by <code>null</code> values.  Indels may occur on the first row, indicating epenthesis, or
 * the second row for deletions.
 * <div align='center'>
 * <p align='left'>e.g.,</p> 
 * <table class='matrix'>
 * 	<tr>
 * 		<td>t<sub><i>1</i></sub></td>
 * 		<td>&#x2205;</td>
 * 		<td>t<sub><i>2</i></sub></td>
 * 		<td>&hellip;</td>
 * 		<td>t<sub><i>n</i></sub></td>
 * 	</tr>
 * 	<tr>
 * 		<td>a<sub><i>1</i></sub></td>
 * 		<td>a<sub><i>2</i></sub></td>
 * 		<td>&hellip;</td>
 * 		<td>a<sub><i>m</i></sub></td>
 * 		<td>&#x2205;</td>
 * 	</tr>
 * </table>
 * </div>
 * </li>
 * <li>There exists a function <i>profile(p)</i>, &#x2200; <i>p</i> &#x2208; &#x2119;, which provides a set of values for the phonetic? dimensions 
 * of <i>p</i>.  <i>profile(p)</i> is defined as:
 * <div>
 * 	<p><i>profile(p)</i> = </p>
 * 	<table class="stepfunction">
 * 		<tr>
 * 			<td>{ <i>place(p)<i>, <i>manner(p)</i>, <i>voicing(p)</i> }</td>
 * 			<td>if <i>p</i> &#x2208; &#x2102;</td>
 * 		</tr>
 * 		<tr>
 * 			<td>{ <i>height(p)</i>, <i>backness(p)</i>, <i>tenseness(p)</i>, <i>rounding(p)</i> }</td>
 * 			<td>if <i>p</i> &#x2208; &#x1d54d;</td>
 * 		</tr>
 * 		<tr>
 * 			<td>&#x2205;</td>
 * 			<td>Otherwise</td>
 * 		</tr>
 * 	</table>
 * </p>
 * <p>In most functions, dimensions are treated independently and the virtual function <i>dim(p)</i> is used to represent any
 * dimension.</p>
 * </div>
 * </li>
 * </ul>
 * </p>
 * </div>
 * 
 * <div id='harmony'><h2>Harmony</h2>
 * 
 * <p>Given two positions <i>i</i>, <i>k</i> within M, we determine if harmony exists for each dimension of <i>profile(p)</i> if any of the following cases are true:
 * <ul>
 * 	<li>
 * 		<b>Progressive Harmony</b><br/>
 * 		<i>dim(M<sub><i>1i</i></sub>)</i> = <i>x</i>, <i>dim(M<sub><i>1k</i></sub>)</i> = <i>y</i> (Values from T)<br/>
 * 		<i>dim(M<sub><i>2i</i></sub>)</i> = <i>x</i>, <i>dim(M<sub><i>2k</i></sub>)</i> = <i>x</i> (Values from A)<br/>
 * 	</li>
 * 	<li>
 * 		<b>Regressive Harmony</b><br/>
 * 		<i>dim(M<sub><i>1i</i></sub>)</i> = <i>x</i>, <i>dim(M<sub><i>1k</i></sub>)</i> = <i>y</i> (Values from T)<br/>
 * 		<i>dim(M<sub><i>2i</i></sub>)</i> = <i>y</i>, <i>dim(M<sub><i>2k</i></sub>)</i> = <i>y</i> (Values from A)<br/>
 * 	</li>
 * 	<li>
 * 		<b>Progressive Harmony?</b><br/>
 * 		<i>dim(M<sub><i>1i</i></sub>)</i> = <i>x</i>, <i>dim(M<sub><i>1k</i></sub>)</i> = &#x2205; (Values from T)<br/>
 * 		<i>dim(M<sub><i>2i</i></sub>)</i> = <i>x</i>, <i>dim(M<sub><i>2k</i></sub>)</i> = <i>x</i> (Values from A)<br/>
 * 	</li>
 *  <li>
 * 		<b>Regressive Harmony?</b><br/>
 * 		<i>dim(M<sub><i>1i</i></sub>)</i> = &#x2205;, <i>dim(M<sub><i>1k</i></sub>)</i> = <i>x</i> (Values from T)<br/>
 * 		<i>dim(M<sub><i>2i</i></sub>)</i> = <i>x</i>, <i>dim(M<sub><i>2k</i></sub>)</i> = <i>x</i> (Values from A)<br/>
 * 	</li>
 * </ul>
 * </p>
 * 
 * </div>
 */
package ca.phon.query.detectors;
