#
# spec file for package MediaInfoFetcher 1.0
#
# Copyright (C) 2008  John-Paul.Stanford <dev@stanwood.org.uk>
# This file and all modifications and additions to the pristine
# package are under the same license as the package itself.
#
# Please submit bugfixes or comments via http://code.google.com/p/tv-and-movies-meta-data-fetcher/
#
Name:           MediaInfoFetcher
Requires:       java >= 1.6
Requires:       jpackage-utils
Requires:       jakarta-commons-cli = 1.0
Requires:		jakarta-commons-lang >= 2.6
Requires:       jericho-html >= 2.6
Requires:       log4j >= 1.2.15
Requires:       jakarta-commons-logging >= 1.0.4
Requires:       mysql-connector-java >= 5.1.6
Requires:       jericho-html >= 2.6
Requires:		antlr-java >= 3.1.3
Requires:		jaudiotagger >= 1.0.9
BuildRequires:  jpackage-utils
BuildRequires:  unzip
BuildRequires:  jericho-html >= 2.6
BuildRequires:  jakarta-commons-cli = 1.0
BuildRequires:	jakarta-commons-lang >= 2.6
BuildRequires:  java >= 1.6
BuildRequires:  java-devel >= 1.6
BuildRequires:  jakarta-commons-logging >= 1.0.4
BuildRequires:  log4j >= 1.2.15
BuildRequires:  ant
BuildRequires:  fop >= 0.95
BuildRequires:  ant-trax
BuildRequires:  xalan-j2
BuildRequires:	antlr-java >= 3.1.3
BuildRequires:	jaudiotagger >= 1.0.9
%if 0%{?suse_version} >= 1140
BuildRequires:  excalibur-avalon-framework >= 4.3.1
BuildRequires:  xmlgraphics-batik >= 1.7
%endif
Summary:        A application for correcting the name of TV shows and films
Version:        %%version%%
Release:        %%release%%
License:        GPL
Group:          Applications/Internet
URL:            http://code.google.com/p/tv-and-movies-meta-data-fetcher/
Source:         http://tv-and-movies-meta-data-fetcher.googlecode.com/files/%%sourcefile%%
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch
 
%description
%%description%%
 
Authors
-------
  John-Paul Stanford <dev@stanwood.org.uk>
 
%package javadoc
Summary:    Javadoc for MediaInfoFetcher
Group:      Documentation/HTML
PreReq:     coreutils
 
%description javadoc
Javadoc for MediaInfoFetcher application and API.
 
%prep
%setup -q
 
%build
export CLASSPATH=$CLASSPATH:/usr/share/java/xalan-j2-serializer.jar
%if 0%{?suse_version} >= 1140
%ant -buildfile opensuse-11.4-build.xml \
     -Dlib.dir=%{_javadir} \
     -Dproject.version=%{version} all
%else
%ant -buildfile opensuse-build.xml \
     -Dlib.dir=%{_javadir} \
     -Dproject.version=%{version} \
     -Dfop.dir=/usr/share/fop/lib all
%endif
 
%install
export NO_BRP_CHECK_BYTECODE_VERSION=true
%__install -dm 755 %{buildroot}%{_javadir}
%__install -dm 755 %{buildroot}%{_bindir}
%__install -dm 755 %{buildroot}/etc
%__install -m 644 dist/%{name}-%{version}.jar %{buildroot}%{_javadir}/%{name}-%{version}.jar
pushd %{buildroot}%{_javadir}
    for jar in *-%{version}*; do
        ln -sf ${jar} `echo $jar| sed "s|-%{version}||g"`
    done
popd
%__install -m 755 scripts/opensuse-11.0-mm-renamer %{buildroot}%{_bindir}/mm-renamer
%__install -m 755 scripts/opensuse-11.0-mm-xbmc %{buildroot}%{_bindir}/mm-xbmc
%__install -m 644 etc/defaultConfig.xml %{buildroot}/etc/mediafetcher-conf.xml
 
# User docs
%__install -dm 755 %{buildroot}/usr/share/doc/%{name}
%__install -m 644 docs/userguide/userguide.pdf %{buildroot}/usr/share/doc/%{name}
%__install -m 644 docs/userguide/html/docbook.html %{buildroot}/usr/share/doc/%{name}/userguide.html
%__install -dm 755 %{buildroot}%{_datadir}/applications
%__install -m 644 build/shortcuts/Media\ Manager\ CLI\ Documentation\ \(HTML\).desktop %{buildroot}%{_datadir}/applications/
%__install -m 644 build/shortcuts/Media\ Manager\ CLI\ Documentation\ \(PDF\).desktop %{buildroot}%{_datadir}/applications/
 
# javadoc
%__install -dm 755 %{buildroot}%{_javadocdir}/%{name}-%{version}
%__cp -pr docs/api/* %{buildroot}%{_javadocdir}/%{name}-%{version}
ln -s %{name}-%{version} %{buildroot}%{_javadocdir}/%{name} # ghost symlink
 
%clean
[ -d "%{buildroot}" -a "%{buildroot}" != "" ] && %__rm -rf "%{buildroot}"
 
%post javadoc
%__rm -f %{_javadocdir}/%{name}
ln -s %{name}-%{version} %{_javadocdir}/%{name}
 
%files
%defattr(-,root,root)
%{_javadir}/*.jar
%{_bindir}/mm-renamer
%{_bindir}/mm-xbmc
%{_datadir}/applications/*.desktop
%dir /usr/share/doc/%{name}
%doc /usr/share/doc/%{name}/*
%config /etc/mediafetcher-conf.xml

%files javadoc
%defattr(-,root,root)
%doc %{_javadocdir}/%{name}-%{version}
%ghost %doc %{_javadocdir}/%{name}
 
%changelog
%%changelog%%
