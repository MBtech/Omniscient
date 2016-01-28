/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.learningcurve.mb.omniscient;

import com.sun.tools.attach.VirtualMachine;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Set;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 *
 * @author mb
 */
// How to run: java -classpath "/Library/Java/JavaVirtualMachines/jdk1.8.0_60.jdk/Contents/Home/lib/tools.jar:Omniscient-jar-with-dependencies.jar" com.learningcurve.mb.omniscient.jvmMonitor 30904/pid
public class JVMMonitor {
    public static void main(String args[]) throws Exception {
    if (args.length != 1) {
      System.err.println("Please provide process id");
      System.exit(-1);
    }
    VirtualMachine vm = VirtualMachine.attach(args[0]);
    String connectorAddr = vm.getAgentProperties().getProperty(
      "com.sun.management.jmxremote.localConnectorAddress");
    if (connectorAddr == null) {
      String agent = vm.getSystemProperties().getProperty(
        "java.home")+File.separator+"lib"+File.separator+
        "management-agent.jar";
      vm.loadAgent(agent);
      connectorAddr = vm.getAgentProperties().getProperty(
        "com.sun.management.jmxremote.localConnectorAddress");
    }
    JMXServiceURL serviceURL = new JMXServiceURL(connectorAddr);
    JMXConnector connector = JMXConnectorFactory.connect(serviceURL); 
    MBeanServerConnection mbsc = connector.getMBeanServerConnection(); 
    ObjectName objName = new ObjectName(
      ManagementFactory.THREAD_MXBEAN_NAME);
    Set<ObjectName> mbeans = mbsc.queryNames(objName, null);
    for (ObjectName name: mbeans) {
      ThreadMXBean threadBean;
      threadBean = ManagementFactory.newPlatformMXBeanProxy(
        mbsc, name.toString(), ThreadMXBean.class);
      long threadIds[] = threadBean.getAllThreadIds();
      for (long threadId: threadIds) {
        ThreadInfo threadInfo = threadBean.getThreadInfo(threadId);
        System.out.println (threadInfo.getThreadName() + " / " +
            threadInfo.getThreadState());
      }
    }
  }
}
