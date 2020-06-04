Name:           jfrog-artifactory-oss
Provides:       artifactory = %{artifactory_version}
Obsoletes:      artifactory >= 3.6.0
Conflicts:      jfrog-artifactory-pro jfrog-artifactory-vrcs jfrog-artifactory-cpp-ce artifactory < 3.6.0
Version:        %{artifactory_version}
Release:        %{artifactory_release}
Summary:        Binary Repository Manager
Vendor:         JFrog Ltd.
Group:          Development/Tools
License:        AGPL-V3
URL:            http://www.jfrog.org
Source0:        standalone.zip
BuildRoot:      %{_tmppath}/build-%{name}-%{version}
BuildArch:      noarch
Requires:       %{_sbindir}/useradd, %{_sbindir}/groupadd, %{_bindir}/pkill, %{_bindir}/rsync net-tools

%define username artifactory
%define group_name artifactory
%define extracted_standalone %{_sourcedir}/*artifactory*
%define extracted_tomcat %{extracted_standalone}/tomcat
%define _rpmfilename %{filename_prefix}-%{full_version}.rpm

%define _binaries_in_noarch_packages_terminate_build 0

%define target_jfrog_home /opt/jfrog
%define target_artifactory_install %{target_jfrog_home}/artifactory
%define target_jfrog_doc %{target_jfrog_home}/doc/artifactory-%{full_version}
%define	target_tomcat_home %{target_artifactory_install}/tomcat
%define target_etc_dir /etc%{target_artifactory_install}
%define target_var_dir /var%{target_jfrog_home}
%define target_artifactory_home %{target_var_dir}/artifactory
%define lib_folder /usr/lib

%description
The best binary repository manager around.

%prep
%setup -q -T -c
%{__unzip} "%{SOURCE0}" -d "%{_sourcedir}"

%build

%install
%__rm -rf %{buildroot}

# Install and copy the files in the install dir (opt) directories
%__install -d "%{buildroot}%{target_jfrog_home}"
%__install -d "%{buildroot}%{target_artifactory_install}/bin"
%__install -d "%{buildroot}%{target_artifactory_install}/misc"
%__install -d "%{buildroot}%{target_tomcat_home}/webapps"

# The war files are in Artifactory not tomcat webapps
%__install -D %{extracted_standalone}/webapps/artifactory.war "%{buildroot}%{target_artifactory_install}/webapps/artifactory.war"
%__install -D %{extracted_standalone}/webapps/access.war "%{buildroot}%{target_artifactory_install}/webapps/access.war"

%__cp -r %{extracted_standalone}/bin/* "%{buildroot}%{target_artifactory_install}/bin/"
%__cp -r %{extracted_standalone}/misc/* "%{buildroot}%{target_artifactory_install}/misc/"
rsync -r --exclude 'work' --exclude 'temp'  %{extracted_tomcat}/* "%{buildroot}%{target_tomcat_home}/"
%__cp %{extracted_standalone}/misc/service/setenv.sh "%{buildroot}%{target_tomcat_home}/bin/"

# Copy the etc dir to the from the build dir to the build root (currently contains default script)
%__install -d "%{buildroot}%{target_etc_dir}"

# Copy the contents of the standalone etc to the artifactory etc dir
%__cp -r %{extracted_standalone}/etc/* "%{buildroot}%{target_etc_dir}"
%__install -D %{extracted_standalone}/bin/artifactory.default "%{buildroot}%{target_etc_dir}/default"
%__install -D %{extracted_standalone}/misc/service/artifactory "%{buildroot}%{_sysconfdir}/init.d/artifactory"
%__install -D %{extracted_standalone}/misc/service/artifactory.service "%{buildroot}%{lib_folder}/systemd/system/artifactory.service"

# Replace the vars in the init and default scripts
%__sed -r --in-place "s%#export ARTIFACTORY_HOME=.*%export ARTIFACTORY_HOME=%{target_artifactory_home}%g;" "%{buildroot}%{target_etc_dir}/default"
%__sed -r --in-place "s%export TOMCAT_HOME=.*%export TOMCAT_HOME=%{target_tomcat_home}%g;" "%{buildroot}%{target_etc_dir}/default"
%__sed -r --in-place "s/#export ARTIFACTORY_USER=.*/export ARTIFACTORY_USER=artifactory/g;" "%{buildroot}%{target_etc_dir}/default"
%__sed -r --in-place "s%export ARTIFACTORY_PID=.*%export ARTIFACTORY_PID=/var/run/artifactory.pid%g;" "%{buildroot}%{target_etc_dir}/default"
%__sed -r --in-place "s%Dartdist=zip%Dartdist=rpm%g;" "%{buildroot}%{target_etc_dir}/default"

# Create artifactory home dir (var) and symlinks to install dir (opt)
%__install -d "%{buildroot}%{target_artifactory_home}"
%__install -d "%{buildroot}%{target_var_dir}/run"

# Link the folders
%__ln_s "%{target_artifactory_install}/webapps" "%{buildroot}%{target_artifactory_home}/webapps"
%__ln_s "%{target_artifactory_home}/temp" "%{buildroot}%{target_tomcat_home}/temp"
%__ln_s "%{target_artifactory_home}/work" "%{buildroot}%{target_tomcat_home}/work"
%__ln_s "/etc/opt/jfrog/artifactory" "%{buildroot}/var/opt/jfrog/artifactory/etc"
%__ln_s "%{target_artifactory_install}/misc" "%{buildroot}%{target_artifactory_home}/misc"
%__ln_s "%{target_artifactory_install}/tomcat" "%{buildroot}%{target_artifactory_home}/tomcat"

# log directories installation
%__ln_s "%{target_artifactory_home}/logs/catalina" "%{buildroot}%{target_tomcat_home}/logs"

# Fill the documentation
%__install -d "%{buildroot}%{target_jfrog_doc}"
%__cp %{extracted_standalone}/Third-Parties-Usage-About-Box.html "%{buildroot}%{target_jfrog_doc}"
%__cp %{extracted_standalone}/COPYING.AFFERO "%{buildroot}%{target_jfrog_doc}"
%__cp %{extracted_standalone}/README.txt "%{buildroot}%{target_jfrog_doc}"

%clean
%__rm -rf %{_builddir}/artifactory-%{version}

%pre

CURRENT_USER=$(id -nu)
if [ "$CURRENT_USER" != "root" ]; then
    echo
    echo "ERROR: Please install Artifactory using root."
    echo
    exit 1
fi

SERVICE_FILE="%{_sysconfdir}/init.d/artifactory"
SERVICE_SYSTEMD_FILE="%{lib_folder}/systemd/system/artifactory.service"

if [ -e "$SERVICE_FILE" ]; then
    # Shutting down the artifactory service if running
    SERVICE_STATUS="$(${SERVICE_FILE} status)"
    if [[ ! "$SERVICE_STATUS" =~ .*[sS]topped.* ]]; then
        echo "Stopping the artifactory service..."
        ${SERVICE_FILE} stop || exit $?
    fi
fi

if [ -e "$SERVICE_SYSTEMD_FILE" ]; then
    # Shutting down the artifactory service if running
    awk -F/ '$2 == "docker"' /proc/self/cgroup | read
    DOCKER_VERIFICATION=$?
    if [[ ${DOCKER_VERIFICATION} -eq 0 ]] ; then
        # Shutting down the artifactory service if running
        ARTIFACTORY_MANAGE_FILE=%{target_artifactory_install}/bin/artifactoryManage.sh
        SERVICE_STATUS="$($ARTIFACTORY_MANAGE_FILE status)"
        if [[ ! "$SERVICE_STATUS" =~ .*[sS]topped.* ]]; then
            echo "Stopping the artifactory service..."
            $ARTIFACTORY_MANAGE_FILE stop || exit $?
        fi
    else
        # Shutting down the artifactory service if running
        SERVICE_STATUS="$(systemctl status artifactory.service)"
        if [[ ! "$SERVICE_STATUS" =~ .*[sS]topped.* ]]; then
            echo "Stopping the artifactory service..."
            systemctl stop artifactory.service || exit $?
        fi
    fi
fi

[ -e /etc/opt/jfrog/artifactory ] && read uid gid <<<$(stat -c '%u %g' /etc/opt/jfrog/artifactory)

if [ "$1" = "1" ]; then
    # Initial installation
    echo "Checking if group %{group_name} exists..."
    getent group "%{group_name}" 2>&1 1>/dev/null
    if [ $? != 0  ]; then
      echo "Group %{group_name} doesn't exist. Creating ..."
      %{_sbindir}/groupadd -r %{group_name} ${gid:+-g} $gid|| exit $?
    else
      echo "Group %{group_name} exists."
    fi

    echo "Checking if user %{username} exists..."
    getent passwd "%{username}" 2>&1 1>/dev/null
    if [ $? != 0 ]; then
      echo "User %{username} doesn't exist. Creating ..."
      %{_sbindir}/useradd %{username} -g %{username} -M -s /usr/sbin/nologin ${uid:+-u} $uid || exit $?
    else
      echo "User %{username} exists."
    fi

elif [ "$1" = "2" ]; then
    # Upgrade
    if [ -f %{target_etc_dir}/default ]; then
        username=$(grep ARTIFACTORY_USER %{target_etc_dir}/default | awk '{print $2}' | awk -F '=' '{print $2}')
        group_name=$(grep ARTIFACTORY_USER %{target_etc_dir}/default | awk '{print $2}' | awk -F '=' '{print $2}')
    fi
fi

echo "Checking if ARTIFACTORY_HOME exists"
if [ ! -d "%{target_artifactory_home}" ]; then
  %__mkdir_p %{target_artifactory_home}
fi

# Cleaning the artifactory webapp and work folder
echo "Removing tomcat work directory"
if [ -d %{target_tomcat_home}/work ]; then
  %__rm -rf %{target_tomcat_home}/work || exit $?
fi

if [ -d %{target_tomcat_home}/webapps/artifactory ]; then
  echo "Removing Artifactory's exploded WAR directory"
  %__rm -rf %{target_tomcat_home}/webapps/artifactory || exit $?
fi
exit 0

%post

SERVICE_INIT_FILE="%{_sysconfdir}/init.d/artifactory"
SERVICE_SYSTEMD_FILE="%{lib_folder}/systemd/system/artifactory.service"
SERVICE_TYPE=""
TIMESTAMP=$(echo "$(date '+%T')" | tr -d ":")
CURRENT_TIME=$(date '+%Y%m%d').$TIMESTAMP

if [ "$1" = "1" ]; then
  # This is the first install
  echo "Adding the artifactory service to auto-start"
  awk -F/ '$2 == "docker"' /proc/self/cgroup | read
  DOCKER_VERIFICATION=$?
  systemctl -h > /dev/null 2>&1
  if [[ $? -eq 0 ]] && [[ ${DOCKER_VERIFICATION} -gt 0 ]] ; then
        # File SERVICE_INIT_FILE exists, removing
        if [ -e ${SERVICE_INIT_FILE} ]; then
            rm -f ${SERVICE_INIT_FILE}
        fi
        systemctl daemon-reload &>/dev/null
        systemctl enable artifactory.service &>/dev/null
        systemctl list-unit-files --type=service | grep artifactory.service &>/dev/null || errorArtHome "Could not install artifactory service"
        echo -e " DONE"
        SERVICE_TYPE="systemd"
  else
        # File SERVICE_SYSTEMD_FILE exists, removing since no systemd supported
        if [ -e ${SERVICE_SYSTEMD_FILE} ]; then
            rm -f ${SERVICE_SYSTEMD_FILE}
        fi
        /sbin/chkconfig --list artifactory 2>&1 1>/dev/null || /sbin/chkconfig --add artifactory
        echo -e " DONE"
        SERVICE_TYPE="init.d"
  fi
  echo
  echo "NOTE: Updating the ownership of files and directories. This may take several minutes. Do not stop the installation process."
  chown -R %{username}: %{target_tomcat_home}/webapps
  chown -R %{username}: %{target_artifactory_home}
  chown -R %{username}: %{target_var_dir}/run/
  chown -R %{username}: /etc%{target_jfrog_home}
  chown %{username}: %{target_etc_dir}/artifactory.system.properties
  chown %{username}: %{target_etc_dir}/artifactory.config.xml
  chown %{username}: %{target_etc_dir}/binarystore.xml
  chown %{username}: %{target_etc_dir}/default
  chown %{username}: %{target_etc_dir}/logback.xml
  chown %{username}: %{target_etc_dir}/mimetypes.xml
  echo
  echo "The installation of Artifactory has completed successfully."
  echo
  echo "PLEASE NOTE: It is highly recommended to use Artifactory with an external database (MySQL, Oracle, Microsoft SQL Server, PostgreSQL, MariaDB). For details about how to configure the database, refer to https://www.jfrog.com/confluence/display/RTF/Configuring+the+Database"
  echo
    if [ ${SERVICE_TYPE} == "init.d" ]; then
        echo "You can now check installation by running:"
        echo "> service artifactory check (or $SERVICE_INIT_FILE check)"
        echo
        echo "Then activate artifactory with:"
        echo "> service artifactory start (or $SERVICE_INIT_FILE start)"
    fi
    if [ ${SERVICE_TYPE} == "systemd" ]; then
        echo "Start Artifactory with:"
        echo "> systemctl start artifactory.service"
        echo
        echo "Check Artifactory status with:"
        echo "> systemctl status artifactory.service"
    fi
elif [ $1 -gt 1 ]; then
    # This is an upgrade install"
    awk -F/ '$2 == "docker"' /proc/self/cgroup | read
    DOCKER_VERIFICATION=$?
    systemctl -h > /dev/null 2>&1
    if [[ $? -eq 0 ]] && [[ ${DOCKER_VERIFICATION} -gt 0 ]] ; then
        if [ -e ${SERVICE_INIT_FILE} ]; then
            mv ${SERVICE_INIT_FILE} ${SERVICE_INIT_FILE}.disabled
        fi
        # Update new systemd artifactory.service with actual pid file location on upgrade to keep existing location
        pidfile=$(grep ARTIFACTORY_PID %{target_etc_dir}/default | awk '{print $2}' | awk -F '=' '{print $2}')
        # Check if the PIDFile needs to be updated
        if ! grep "${pidfile}" ${SERVICE_SYSTEMD_FILE} > /dev/null; then
            echo "Updating ${SERVICE_SYSTEMD_FILE} with current PIDFile value (${pidfile})"
            cp -f ${SERVICE_SYSTEMD_FILE} %{target_etc_dir}/artifactory.service.backup.${CURRENT_TIME}
            echo "Saved old ${SERVICE_SYSTEMD_FILE} as %{target_etc_dir}/artifactory.service.backup.${CURRENT_TIME}"
            sed -i "s,^PIDFile=.*,PIDFile=${pidfile},g" ${SERVICE_SYSTEMD_FILE}
        fi
        echo "Initializing artifactory service with systemctl..."
        systemctl daemon-reload &>/dev/null
        systemctl enable artifactory.service &>/dev/null
        systemctl list-unit-files --type=service | grep artifactory.service &>/dev/null || errorArtHome "Could not install artifactory service"
        SERVICE_TYPE="systemd"
    else
        if [ -e ${SERVICE_SYSTEMD_FILE} ]; then
            rm -f ${SERVICE_SYSTEMD_FILE}
        fi
        /sbin/chkconfig --list artifactory 2>&1 1>/dev/null || /sbin/chkconfig --add artifactory
        SERVICE_TYPE="init.d"
    fi
  echo
  echo -e "\033[33m************ SUCCESS ****************\033[0m"
  echo -e "\033[33mThe upgrade of Artifactory has completed successfully.\033[0m"
  echo
    if [ ${SERVICE_TYPE} == "init.d" ]; then
        echo "You can now check installation by running:"
        echo "> service artifactory check (or $SERVICE_INIT_FILE check)"
        echo
        echo "Then activate artifactory with:"
        echo "> service artifactory start (or $SERVICE_INIT_FILE start)"
    fi
    if [ ${SERVICE_TYPE} == "systemd" ]; then
        echo "Start Artifactory with:"
        echo "> systemctl start artifactory.service"
        echo
        echo "Check Artifactory status with:"
        echo "> systemctl status artifactory.service"
    fi

  username=$(grep ARTIFACTORY_USER %{target_etc_dir}/default | awk '{print $2}' | awk -F '=' '{print $2}')
  echo
  echo "NOTE: Updating the ownership of files and directories. This may take several minutes. Do not stop the installation/upgrade process."
  chown ${username}: %{target_tomcat_home}
  chown -h ${username}: %{target_artifactory_home} %{target_artifactory_home}/*
  chown -R ${username}: %{target_tomcat_home}/webapps
  chown -R ${username}: %{target_var_dir}/run/
  chown -R ${username}: /etc%{target_jfrog_home}
  echo
fi

exit 0

%preun
if [ "$1" = "0" ]; then
  # It's an un-installation

  CURRENT_USER=$(id -nu)
  if [ "$CURRENT_USER" != "root" ]; then
    echo
    echo "ERROR: Please un-install Artifactory using root."
    echo
    exit 1
  fi

  SERVICE_FILE="%{_sysconfdir}/init.d/artifactory"
  SERVICE_SYSTEMD_FILE="/etc/systemd/system/artifactory.service"
  SERVICE_SYSTEMD_LIB_FILE="/usr/lib/systemd/system/artifactory.service"

  if [ -f $SERVICE_FILE ]; then
    SERVICE_STATUS="$(${SERVICE_FILE} status)"
    if [[ ! "$SERVICE_STATUS" =~ .*[sS]topped.* ]]; then
      echo "Stopping the artifactory service..."
      ${SERVICE_FILE} stop || exit $?
    fi
    echo -n "Removing the artifactory service from auto-start..."
    /sbin/chkconfig --del artifactory 2>/dev/null
    if [ -f ${SERVICE_FILE} ]; then
        rm -f ${SERVICE_FILE} || exit "Could not delete $SERVICE_FILE"
    fi
    if [ -f ${SERVICE_FILE}.disabled ]; then
        rm -f ${SERVICE_FILE}.disabled || exit "Could not delete ${SERVICE_FILE}.disabled"
    fi
    echo -e " DONE"
  fi

  if [ -f ${SERVICE_SYSTEMD_FILE} ]; then
    SERVICE_STATUS="$(systemctl status artifactory.service)"
    if [[ ! "$SERVICE_STATUS" =~ .*[sS]topped.* ]]; then
      echo "Stopping the artifactory service..."
      systemctl stop artifactory.service || exit $?
    fi
    echo -n "Removing the artifactory service from auto-start..."
    systemctl disable artifactory.service &>/dev/null
    if [ -f ${SERVICE_SYSTEMD_FILE} ]; then
        rm -f ${SERVICE_SYSTEMD_FILE} || exit "Could not delete $SERVICE_SYSTEMD_FILE"
    fi
    if [ -f ${SERVICE_SYSTEMD_FILE}.disabled ]; then
        rm -f ${SERVICE_SYSTEMD_FILE}.disabled || exit "Could not delete ${SERVICE_SYSTEMD_FILE}.disabled"
    fi
    if [ -f ${SERVICE_SYSTEMD_LIB_FILE} ]; then
        rm -f ${SERVICE_SYSTEMD_LIB_FILE} || error "Could not delete $SERVICE_SYSTEMD_LIB_FILE"
    fi
    echo -e " DONE"
  fi

  # Create backups
  TIMESTAMP=$(echo "$(date '+%T')" | tr -d ":")
  CURRENT_TIME="$(date '+%Y%m%d').$TIMESTAMP"
  BACKUP_DIR="%{target_var_dir}/artifactory.backup.${CURRENT_TIME}"
  echo "Creating a backup of the artifactory home folder in ${BACKUP_DIR}"

  # ignore any failures or the package will be left in an inconsistent state
  %__mkdir_p "${BACKUP_DIR}" && \
  %__cp -a %{target_etc_dir} "${BACKUP_DIR}/etc" && \
  %__mv %{target_artifactory_home}/logs "${BACKUP_DIR}/logs" 2>&1 1>/dev/null

  %__rm -rf "%{target_artifactory_home}/data/tmp" 2>&1 1>/dev/null
  %__rm -rf "%{target_artifactory_home}/data/work" 2>&1 1>/dev/null

  if [ -e %{target_tomcat_home}/lib/mysql-connector-java*.jar ]; then
    echo "MySQL connector found"
    %__cp %{target_tomcat_home}/lib/mysql-connector-java* "${BACKUP_DIR}" 2>/dev/null
  fi
  if [ -e %{target_artifactory_home}/backup ]; then
    %__mv %{target_artifactory_home}/backup "${BACKUP_DIR}/backup" 2>/dev/null
  fi

  # Temp backup %{target_etc_dir}/default for use by "postun" step
  cp -f %{target_etc_dir}/default /tmp/artifactory-default
fi
exit 0

%postun
if [ "$1" = "0" ]; then
  # It's an un-installation
  username=$(grep ARTIFACTORY_USER /tmp/artifactory-default | awk '{print $2}' | awk -F '=' '{print $2}')
  echo "Logging off user ${username}"
  %{_bindir}/pkill -KILL -u ${username}

  %__rm -rf %{target_artifactory_home}/{work,temp} 2>/dev/null

  # Ignoring user folders since the home dir is deleted already by the RPM spec
  echo "Removing local user ${username}"
  ( grep -q ${username} /etc/passwd &&
  %{_sbindir}/userdel ${username} 2>/dev/null) || echo $?

  group_name=${username}
  EXISTING_GROUP="$(grep ${group_name} /etc/group | awk -F ':' '{ print $1 }' 2>/dev/null)"
  if [ "$EXISTING_GROUP" == "${group_name}" ]; then
    echo "Removing group ${group_name}"
    %{_sbindir}/groupdel ${group_name}
  fi

  # check if artifactory user exists (in case another user was default)
  getent passwd %{username} > /dev/null 2&>1
  if [ $? -eq 0 ]; then
    echo "Removing local user %{username}"
    %{_sbindir}/userdel %{username} 2>/dev/null || echo $?
    %{_sbindir}/groupdel ${group_name} 2>/dev/null
  fi

  # Cleanup temp file
  rm -f /tmp/artifactory-default

  echo
  echo -e "\033[33mUninstallation of Artifactory completed\033[0m"
fi
exit 0

%posttrans
echo post transaction %{name} \$1 = $1 >>/tmp/rpminst
[ -e /etc/opt/jfrog/artifactory ] && read uid gid <<<$(stat -c '%u %g' /etc/opt/jfrog/artifactory)

if [ "$1" = "1" ]; then
    # Initial installation
    getent group "%{group_name}" 2>&1 1>/dev/null
    if [ $? != 0  ]; then
      echo "Group %{group_name} doesn't exist. Creating ..."
      %{_sbindir}/groupadd -r %{group_name} ${gid:+-g} $gid || exit $?
    else
      echo "Group %{group_name} exists."
    fi

    getent passwd "%{username}" 2>&1 1>/dev/null
    if [ $? != 0 ]; then
      echo "User %{username} doesn't exist. Creating ..."
      %{_sbindir}/useradd %{username} -g %{username} -M -s /usr/sbin/nologin ${uid:+-u} $uid || exit $?
    else
      echo "User %{username} exists."
    fi
fi

#echo "Checking if ARTIFACTORY_HOME exists"
if [ ! -d "%{target_artifactory_home}" ]; then
  %__mkdir_p %{target_artifactory_home}
fi

exit 0

%triggerpostun -- artifactory
echo trigger post uninstall %{name} \$1 = $1 >>/tmp/rpminst
exit 0

%triggerun -- artifactory
echo trigger uninstall %{name} \$1 = $1 >>/tmp/rpminst
exit 0

%files
%dir %{target_jfrog_doc}
%dir %{target_artifactory_install}
%dir %{target_jfrog_home}/doc
%attr(775,root,root) %config %{_sysconfdir}/init.d/artifactory
%attr(755,root,root) %config %{lib_folder}/systemd/system/artifactory.service
%attr(775,root,root) %config %{target_artifactory_install}/bin
%attr(775,root,root) %config %{target_artifactory_install}/misc
%attr(775,root,root) %config %{target_artifactory_install}/webapps
%attr(774,root,root) %{target_jfrog_doc}/Third-Parties-Usage-About-Box.html
%attr(774,root,root) %{target_jfrog_doc}/COPYING.AFFERO
%attr(774,root,root) %{target_jfrog_doc}/README.txt
%attr(775,root,root) %dir %{target_tomcat_home}
%attr(775,root,root) %config %{target_tomcat_home}/bin
%attr(775,root,root) %config %{target_tomcat_home}/conf
%attr(775,root,root) %{target_tomcat_home}/lib
%{target_tomcat_home}/logs
%{target_tomcat_home}/temp
%{target_tomcat_home}/work
%attr(775,root,root) %{target_artifactory_home}
%attr(775,root,root) %{target_tomcat_home}/webapps
%attr(774,root,root) %{target_tomcat_home}/LICENSE
%attr(774,root,root) %{target_tomcat_home}/NOTICE
%attr(774,root,root) %{target_tomcat_home}/RELEASE-NOTES
%attr(774,root,root) %{target_tomcat_home}/RUNNING.txt

%defattr(770,root,root, -)
%{target_var_dir}/run
%dir %{target_etc_dir}
%config(noreplace) %{target_etc_dir}/artifactory.system.properties
%config(missingok) %{target_etc_dir}/artifactory.config.xml
%config(noreplace) %{target_etc_dir}/binarystore.xml
%config(noreplace) %{target_etc_dir}/default
%config(noreplace) %{target_etc_dir}/logback.xml
%config %{target_tomcat_home}/conf/server.xml
%config %{target_etc_dir}/mimetypes.xml

%doc
