#
# spec file for package MediaInfoFetcher 1.0
#
# Copyright (C) 2008  John-Paul.Stanford <dev@stanwood.org.uk>
# This file and all modifications and additions to the pristine
# package are under the same license as the package itself.
#
# Please submit bugfixes or comments via http://code.google.com/p/tv-and-movies-meta-data-fetcher/
#
%define _topdir @project.rpm.topdir@
Name:           jericho-html
Requires:       java >= 1.6
Requires:		jpackage-utils
BuildRequires:	jpackage-utils
Summary:        Jericho HTML Parser is a java library allowing analysis and manipulation of parts of an HTML document, including server-side tags, while reproducing verbatim any unrecognised or invalid HTML. It also provides high-level HTML form manipulation functions.
Version:        2.6
Release:        1
License:        GPL
Group:          Applications/Internet
URL:            http://code.google.com/p/tv-and-movies-meta-data-fetcher/
Distribution:   OpenSUSE 11.0
Source0:         %{name}-%{version}.jar
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch
Provides:       %{name}
Obsoletes:      %{name}

%description
 Jericho HTML Parser is a java library allowing analysis and manipulation of parts of an HTML document, including server-side tags, while reproducing verbatim any unrecognised or invalid HTML. It also provides high-level HTML form manipulation functions.

It is an open source library released under both the Eclipse Public License (EPL) and GNU Lesser General Public License (LGPL). You are therefore free to use it in commercial applications subject to the terms detailed in either one of these licence documents. 

%prep

%install
%__install -dm 755 %{buildroot}%{_javadir}
%__install -m 644 %{SOURCE0} \
   %{buildroot}%{_javadir}/%{name}-%{version}.jar
pushd %{buildroot}%{_javadir}
	for jar in *-%{version}*; do
		ln -sf ${jar} `echo $jar| sed "s|-%{version}||g"`
	done
popd

%clean
[ -d "%{buildroot}" -a "%{buildroot}" != "" ] && %__rm -rf "%{buildroot}"

%files
%{_javadir}/*.jar

%changelog
* Thu Jun 05 2008 - 2.6   
   - Bug Fixes:
     - [1906051] Exponential recursion when non-server tags are present
       inside attribute values during full seq parse (introduced v2.5).
     - [1927391] Renderer had indenting problems.
     - [1991529] Wrong encoding with DISPLAY_VALUE and select Tags.
     - An element whose start tag and end tag have different names, such
       as a Mason component called with content, had no end tag.
     - SourceFormatter did not preserve original indentation inside server
       tags as specified in documentation.
     - A start tag containing a server tag immediately before its closing
       delimiter was not parsed correctly.
     - StartTag.tidy() removed server tags outside of attribute values.
     - Nested elements formed from non-normal tag types were not parsed
       correctly.
     - CharStreamSourceUtil.toString(charStreamSource) broke if
       charStreamSource.getEstimatedMaximumOutputLength()<-1
   - CHANGES THAT COULD AFFECT THE BEHAVIOUR OF EXISTING PROGRAMS:
     - Non-server tags are no longer recognised inside server tags.
       (see the TagType.isValidPosition documentation for details)
     - Elements inside <script> elements are now ignored up until the first
       occurrence of the character sequence "</script" (previously "</")
       during a full sequential parse.
     - Added static Config.ConvertNonBreakingSpaces property, which
       affects the default behaviour of several methods.
     - StartTag.isEmptyElementTag() now checks that the start tag is not
       one that has an optional or required end tag.
     - Element.isEmptyElementTag() is now implemented to be identical to
       StartTag.isEmptyElementTag().
   - Added StartTag.isSyntacticalEmptyElementTag() method.
   - Improved performance of internal stream writing methods.
   - Added StartTagType.SERVER_COMMON_ESCAPED standard tag type.
   - Added MicrosoftTagTypes.DOWNLEVEL_REVEALED_CONDITIONAL_COMMENT
     extended tag type.
   - Added Source(URLConnection) constructor.
   - Added Source.findNextStartTag(pos,name,startTagType) method.
   - Added Source.findPreviousStartTag(pos,name,startTagType) method.
   - Added SourceCompactor class and CompactSource sample program.
   - Added Segment.getNodeIterator() method.
   - Reduced risk of stack overflow when parsing large documents without
     full sequential parse by avoiding recursive comment search.
   - Added TextExtractor.includeAttribute(StartTag,Attribute) method.
   - TextExtractor now includes attribute contents in order of appearance
     in the source document.
   - TextExtractor now includes contents of href attributes if the
     IncludeAttributes property is set.
   - Added Renderer.IncludeHyperlinkURLs property.
   - Renderer no longer includes A element href if it is equal to "#"
     or starts with "javascript:".
   - Added Segment.getSource() method.
   - Added EndTagType.getEndTagName(String startTagName) method.
   - Added OutputDocument.writeTo(Writer, int begin, int end) method.
   - OutputDocument now ignores output segments enclosed by other
     output segments.
   - FormFields.getDataSet() Map entries are now ordered to match the
     order of appearance of the keys in the source document.
   - FormFields.getValues() now returns a List rather than a Collection.
   - FormField.getValues() now returns a List rather than a Collection.
   - Added WriterLogger.log(String level, String message) method.
   - Upgraded to the following logger APIs:
     slf4j-api-1.5.2, commons-logging-api-1.1.1, log4j-1.2.15
* Sun Sep 02 2007 - 2.5   
   - Bug Fixes:
     - [1747493] RenderToText does not handle multiple <br> correctly.
     - RenderToText does not handle whitespace after <br> correctly.
     - Resetting to invalid mark exception during encoding detection.
     - INPUT elements of type "button" and "reset" incorrectly 
       interpreted as form controls of type FormControlType.TEXT.
     - Valid end tags containing white space rejected.
   - Elements inside <script> elements are now ignored, up until the first
     occurrence of the character sequence "</".
   - Improved encoding detection.
   - Added Source.getPreliminaryEncodingInfo() method.
   - Improved parsing of attributes containing server tags.
   - Changed Source.isXML() algorithm.
   - Added Renderer.ConvertNonBreakingSpaces property.
   - Added TextExtractor.ConvertNonBreakingSpaces property.
   - Added TextExtractor.ExcludeNonHTMLElements property.
   - Added extendible TextExtractor.excludeElement(StartTag) method.
   - TextExtractor now includes value of content attribute.
   - Deprecated OverlappingOutputSegmentsException class.
   - Added OutputDocument.getRegisteredOutputSegments() method.
   - Added OutputDocument.getDebugInfo() method.
   - Added fullSequentialParseData parameter to TagType.isValidPosition.
   - Removed all methods/classes deprecated in 2.2.
* Sun May 20 2007 - 2.4   
   - Released under dual EPL/LGPL licence.
   - Bug Fixes:
     - [1583814] Indent method outputs multiple </script> tags
     - [1576991] Bug in ConvertStyleSheets sample program
     - [1597587] various NPEs in findFormFields()
     - [1599700] Segment.findAllStartTags(attributeName...) infinite loop
     - Overlapping elements resulted in some elements being listed as a
       child of more than one parent element.
     - OutputDocument.writeTo(Writer) closed the writer.
   - Server tags no longer interfere with parsing of start tag attributes.
   - Added Renderer class and Segment.getRenderer() method.
   - Added TextExtractor class and Segment.getTextExtractor() method.
   - Deprecated segment.extractText methods.
   - Added SourceFormatter class and Source.getSourceFormatter() method.
   - Deprecated Source.indent method.
   - Added Logger interface along with the related LoggerProvider
     interface and BasicLoggerProvider and WriterLogger classes.
   - Added Source.setLogger(Logger) and Source.getLogger() methods.
   - Deprecated Source.setLogWriter(Writer) and Source.getLogWriter()
     methods.
   - Added Source.findNextElement(int pos, String attributeName,
       String value, boolean valueCaseSensitive) method.
   - Added Segment.findAllElements(String attributeName, String value,
       boolean valueCaseSensitive) method.
   - Calling the ignoreWhenParsing methods on overlapping segments no
     longer results in an OverlappingOutputSegmentsException.
   - Added CharacterReference.getEncodingFilterWriter(Writer) method.
   - Added CharacterReference.encode(char) method.
   - Added Source.getNewLine() method.
   - Added static Config.NewLine parameter.
   - All text output now uses Config.NewLine instead of hard-coded '\n'.
   - Source.fullSequentialParse() method no longer parses the source again
     if it has already been called.
   - Some methods that require the parsing of the entire source now call
     Source.fullSequentialParse() automatically.
   - Some changes to the output of various getDebugInfo() methods.
   - Added categorised class list in javadoc.
   - Removed all methods/constants deprecated in 2.0.
* Mon Sep 11 2006 - 2.3   
   - Bug Fixes:
     - [1510438] NullPointerException in Source.indent.
     - [1511480] Incorrect detection of non-html element with nested
       empty-element tag of same name.
     - [1547562] Fault in caching mechanism.
     - Source.fullSequentialParse() sometimes resulted in unregistered
       tags being returned in tag searches.
     - Invalid Empty-element tags whose name is in either of the sets
       HTMLElements.getEndTagOptionalElementNames() or
       HTMLElements.getEndTagRequiredElementNames() were rejected by the
       parser if the slash immediately follows the tag name.
     - StartTag.tidy() only included a slash before the closing delimiter
       of the tag if the tag name was in the set of
       HTMLElements.getEndTagForbiddenElementNames().  It now includes the
       slash for all tag names not in getEndTagOptionalElementNames().
   - Source.fullSequentialParse() now clears the cache automatically
     instead of throwing an IllegalStateException if the cache is not
     empty.
   - Changes to behaviour of Source.indent:
     - preserves indenting in SCRIPT elements, server elements,
       HTML comments and CDATA sections.
     - keeps SCRIPT elements, HTML comments, XML declarations,
       XML processing instructions and markup declarations inline.
   - Minor documentation improvements.
* Tue Jun 20 2006 - 2.2   
   - Bug Fixes:
     - Fault in caching mechanism resulted in missed tags in rare
       circumstances. (SubCache.findNextTag method)
     - [1407179] Segment.extractText() threw NullPointerException if
       the last character position was part of a tag.
   - Segment.extractText() now converts some tags to whitespace and
     ignores text inside SCRIPT and STYLE elements.
   - Added Segment.extractText(boolean includeAttributes) option.
   - Added Source.fullSequentialParse() method.
   - Added CharStreamSource interface for dealing with char output.
   - Added Source.indent(String indentText, boolean tidyTags,
      boolean collapseWhiteSpace, boolean indentAllElements) method.
   - Added Segment.getChildElements() method.
   - Added Element.getParentElement() method.
   - Added Element.getDepth() method.
   - Named tag search methods now only return unregistered tags if the
     specified name is not a valid XML tag name.
   - Changed Attributes.DefaultMaxErrorCount system default from 1 to 2.
   - Added EndTag.getElement() method.
   - Added Tag.getElement() abstract method.
   - Added Tag.getNameSegment() method.
   - Added Tag.getUserData() and Tag.setUserData(Object) methods.
   - Added Tag.findNextTag() method.
   - Added Tag.findPreviousTag() method.
   - Added Tag.tidy() and Tag.tidy(boolean toXHTML) methods.
   - Added and renamed many methods in OutputDocument class to make the
     interface more intuitive.
   - Added HTMLElements.getNestingForbiddenElementNames() method.
   - Illegally nested elements with required end tags now terminate at
     start of illegally nested start tag, avoiding possible stack overflow
     in the common case of multiple unterminated <a name=...> elements.
   - Tag search methods called with a pos argument that is out of range
     now return null or empty results rather than throwing an exception.
   - Renamed output(Writer) method in OutputSegment to writeTo(Writer).
   - Deprecated Tag.regenerateHTML() method.
   - Deprecated Source.getNextTagIterator() method.
   - Deprecated AttributesOutputSegment class.
   - Deprecated StringOutputSegment class.
   - Removed BlankOutputSegment class from public API.
   - Removed CharOutputSegment class from public API.
   - Removed IOutputSegment which was deprecated in 2.0.
* Sat Dec 24 2005 - 2.1   
   - Added Source(InputStream) constructor.
   - Added Source(Reader) constructor.
   - Added Source(URL) constructor.
   - Added Source.getEncoding() method.
   - Added Source.getEncodingSpecificationInfo() method.
   - Added Source.isXML() method.
   - Added Source.findNextElement(pos) method.
   - Added Source.findNextElement(pos,name) method.
   - Added Segment.extractText() method.
   - Added StartTag.getAttributeValue(attributeName) method.
   - Added Element.getAttributeValue(attributeName) method.
   - Added ExtractText and SourceEncoding sample programs.
* Thu Nov 10 2005 - 2.0   
   - Complete rewrite of the parsing engine to allow the encapsulation of
     different tag types into the new TagType class.
   - Requires Java 1.4 or later.
   - All programs written for previous versions of the library will have
     to be recompiled with the new version, regardless of whether any
     changes are required.  This is because several methods, including the
     Source constructor, now expect a CharSequence as an argument instead
     of a String.
   - Changes that could require modifications to existing programs:
     - The toString() method of Segment and all subclasses now returns the
       source text of the segment instead of a string useful for debugging
       purposes.  This change was necessary because Segment now
       implements CharSequence.
     - For consistency, the toString() methods of all IOutputSegment
       implementations now return the output string instead of a string
       useful for debugging purposes.
     - The return type of the OutputDocument.getSourceText() method is now
       CharSequence instead of String.
     - Character references in Attribute.getValue() are now decoded
     - StartTag.isEmptyElementTag() no longer checks whether the end tag
       is required.
     - Element.getContent() now returns zero-length segment instead of null
       in case of an empty element.
     - FormField.getPredefinedValues() now returns an empty collection
       instead of null if the form field has no predefined values.
     - Segment.findAllStartTags() now returns server tags that are found
       inside other tags.
     - Attributes segment now ends immediately after the last attribute
       instead of immediatley before the end-of-tag delimiter.
     - Modified Segment.isWhiteSpace(char) to match HTML specification
     - CharacterReference.encode(CharSequence) no longer encodes
       apostrophes by default
     - Tags of type SERVER_COMMON now always have the name "%" regardless
       of whether an identifier immediately follows it.
     - Modified and enhanced aspects of StartTag searches relating to
       special tags
     - P elements are now terminated by TABLE elements.
       See the HTMLElementName.P documentation for more information.
   - removed public fields in Attribute class that were deprecated in 1.2
   - removed Source.getSourceTextLowerCase() method deprecated in 1.3
   - removed Source.findEnd(int pos, SpecialTag) method which was
     accidentally added as a public method in 1.4
   - Deprecated numerous methods (details in javadoc)
   - Deprecated IOutputSegment interface and replaced with OutputSegment
   - Improved caching system
   - Added recognition of markup declarations
   - Added recognition of CDATA sections
   - Added recognition of SGML marked sections
   - Doctype declarations containing markup declarations now supported
   - Segment class now implements CharSequence and Comparable
   - Added getDebugInfo() to Segment and all subclasses to replace the
     previous functionality of the toString() method
   - OutputSegment interface now implements CharSequence
   - Added getDebugInfo() to the OutputSegment interface to replace the
     previous functionality of the toString() method
   - Attributes class now implements List
   - FormFields class now implements Collection
   - Added HTMLElementName interface and HTMLElements class
   - Added RowColumnVector class and associated methods in Source class
   - Added FormControl class
   - Added various methods to the FormField, FormFields and OutputDocument
     classes related to FormControl objects and the manipulation and output
     of form submission values.
   - Added Config and related classes
   - Added TagType class and subclasses
   - Added various tag search methods to the Source and Segment classes
     including searches by TagType, attribute values, and other criteria.
   - Added AttributesOutputSegment class
   - Added Util class
   - Added OverlappingOutputSegmentsException class
   - Added many other methods to existing classes
   - Documentation improvements
* Sat Sep 10 2005 - 1.4.1 
   - Bug Fixes:
     - [1065861] Named StartTag search did not find a tag immediately
       following a comment
     - Unnamed StartTag search did not find a comment if the search starts
       at the first character of the comment
     - Character references in FormField.getPredefinedValues() items were
       not decoded
     - FormControlType.SELECT_SINGLE.allowsMultipleValues() returned false
       instead of the correct value of true, resulting in the same
       incorrect value from FormField.allowMultipleValues() when multiple
       SELECT_SINGLE controls with the same name were present in the form
* Fri Sep 02 2004 - 1.4
   - Added CharacterEntityReference and NumbericCharacterReference classes
   - Added CharOutputSegment class
   - Attributes allow whitespace around '=' sign
   - Added convenience method Element.getAttributes()
   - Some documentation improvements
* Sun Jul 25 2004 - 1.3
   - Deprecated Source.getSourceTextLowerCase()
   - Added ignoreWhenParsing methods to Source and Segment classes
     (See sample called JSPTest)
   - Added parseAttributes methods to Source, Segment and StartTag classes
   - Added ability to search for tags in a specified namespace
   - Added BlankOutputSegment class
   - Fixed bug relating to HTML comments with alphabetic characters
     immediately following the opening <!-- characters
* Wed Jun 16 2004 - 1.2
   - Deprecated public fields in Attribute class in favour of accessor
     methods
   - Following methods return empty list instead of null if no result:
     (WARNING - This could possibly break existing programs)
      Segment.findAllStartTags(String name)
      Segment.findAllComments()
      Segment.findAllElements(String name)
      Segment.findAllElements()
   - Added hashCode() method to Segment class
   - Server tags such as ASP, JSP, PSP, PHP and Mason are now recognised
   - Basic parser logging introduced (see Source.setLogWriter() method)
   - Start tags with too many badly formed attributes rejected
     (reduces number of false positives when searching for start tags)
   - Added public IOutputSegment.COMPARATOR field
   - Improved caching
* Sun Mar 07 2004 - 1.1
   - All elements defined in HTML 4.01 are recognised and their properties
     used to aid analysis
   - StartTag.getElement() method enhanced to return the correct span of
     elements which have a missing optional end tag
   - StartTag.isEndTagForbidden() method enhanced to also check the name of
     the tag against the list of elements in the HTML spec whose end tags
     are forbidden
   - Numerous new methods
   - Huge performance enhancement from the use of internal caching
   - Bug Fixes:
     [909944] Parser does not work with unclosed comments.
* Sat Feb 07 2004 - 1.0 
   - Initial Release