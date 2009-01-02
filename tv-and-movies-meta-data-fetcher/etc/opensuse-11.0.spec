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
Name:           @project.name@
Requires:       java >= 1.6
Requires:		jpackage-utils
Requires:		jakarta-commons-cli = 1.0
Requires:       jericho-html >= 2.6
Requires:       log4j >= 1.2.15
Requires:       jakarta-commons-logging >= 1.0.4
BuildRequires:	jpackage-utils 
BuildRequires:	unzip
Summary:        @project.summary@
Version:        @project.version@
Release:        1
License:        GPL
Group:          Applications/Internet
URL:            http://code.google.com/p/tv-and-movies-meta-data-fetcher/
Distribution:   OpenSUSE 11.0
Source:         %{name}-%{version}-bin.zip
Source1:        %{name}-%{version}-apidocs.zip
BuildRoot:      %{_tmppath}/%{name}-%{version}-build
BuildArch:      noarch
Provides:       %{name}
Obsoletes:      %{name}

%description
@project.description@

%package javadoc
Summary:	Javadoc for @project.name@
Group:		Documentation/HTML
PreReq:		coreutils

%description javadoc
Javadoc for @project.name@.

%prep
%setup -q -c -n %{name}
%setup -T -D -a 1 -q -c -n %{name}

%install
%__install -dm 755 %{buildroot}%{_javadir}
%__install -dm 755 %{buildroot}%{_bindir}
%__install -dm 755 %{buildroot}/etc
%__install -m 644 %{name}-%{version}/%{name}-%{version}.jar \
   %{buildroot}%{_javadir}/%{name}-%{version}.jar
pushd %{buildroot}%{_javadir}
	for jar in *-%{version}*; do
		ln -sf ${jar} `echo $jar| sed "s|-%{version}||g"`
	done
popd
%__install -m 755 %{name}-%{version}/opensuse-11.0-media-renamer %{buildroot}%{_bindir}/media-renamer
%__install -m 755 %{name}-%{version}/mediafetcher-conf.xml %{buildroot}/etc/mediafetcher-conf.xml

# javadoc
%__install -dm 755 %{buildroot}%{_javadocdir}/%{name}-%{version}
%__cp -pr api/* %{buildroot}%{_javadocdir}/%{name}-%{version}
ln -s %{name}-%{version} %{buildroot}%{_javadocdir}/%{name} # ghost symlink

%clean
[ -d "%{buildroot}" -a "%{buildroot}" != "" ] && %__rm -rf "%{buildroot}"

%post javadoc
%__rm -f %{_javadocdir}/%{name}
ln -s %{name}-%{version} %{_javadocdir}/%{name}

%files
%{_javadir}/*.jar
%{_bindir}/media-renamer
/etc/mediafetcher-conf.xml

%files javadoc
%defattr(-,root,root)
%doc %{_javadocdir}/%{name}-%{version}
%ghost %doc %{_javadocdir}/%{name}

%changelog
@project.changelog@