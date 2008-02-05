%define version @VERSION@

Summary: Thingamablog, Desktop blogging application
License: GPL
Group: Internet
Name: thingamablog
Provides: thingamablog
Release: 0
Source: thingamablog-%{version}.tar
URL: http://thingamablog.sourceforge.net
Version: %{version}
BuildRoot: /tmp/thingamablog

%description
A cross-platform, standalone, blogging application and news reader 

%prep
rm -rf $RPM_BUILD_ROOT

%setup

%build

%install
install -m 0755 -d $RPM_BUILD_ROOT/opt/thingamablog-%{version}
install -m 0755 -d $RPM_BUILD_ROOT/opt/thingamablog-%{version}/lib
install -m 0755 -d $RPM_BUILD_ROOT/opt/thingamablog-%{version}/dictionaries
install -m 0755 -d $RPM_BUILD_ROOT/opt/thingamablog-%{version}/templates
install -m 0755 -d $RPM_BUILD_ROOT/usr/bin
install -m 0644 thingamablog.jar $RPM_BUILD_ROOT/opt/thingamablog-%{version}/
install -m 0755 run.sh $RPM_BUILD_ROOT/opt/thingamablog-%{version}/
install -m 0644 README.txt $RPM_BUILD_ROOT/opt/thingamablog-%{version}/
install -m 0644 license.txt $RPM_BUILD_ROOT/opt/thingamablog-%{version}/
install -m 0644 lib/* $RPM_BUILD_ROOT/opt/thingamablog-%{version}/lib/
install -m 0644 dictionaries/* $RPM_BUILD_ROOT/opt/thingamablog-%{version}/dictionaries/
cp -ra templates/* $RPM_BUILD_ROOT/opt/thingamablog-%{version}/templates/
ln -sf /opt/thingamablog-%{version}/run.sh $RPM_BUILD_ROOT/usr/bin/thingamablog


%clean
#rm -rf $RPM_BUILD_ROOT

%files
%defattr(-,root,root)
%doc README.txt license.txt
%attr(0644,root,root) /opt/thingamablog-%{version}/thingamablog.jar
%attr(0755,root,root) /opt/thingamablog-%{version}
%attr(0755,root,root) /opt/thingamablog-%{version}/*
%attr(0755,root,root) /opt/thingamablog-%{version}/run.sh
%attr(0755,root,root) /usr/bin/thingamablog

