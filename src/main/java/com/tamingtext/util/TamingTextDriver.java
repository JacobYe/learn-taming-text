/*
 * Copyright 2008-2011 Grant Ingersoll, Thomas Morton and Drew Farris
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * -------------------
 * To purchase or learn more about Taming Text, by Grant Ingersoll, Thomas Morton and Drew Farris, visit
 * http://www.manning.com/ingersoll
 */

package com.tamingtext.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.hadoop.util.ProgramDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * General-purpose driver class for Taming Text programs.  Utilizes org.apache.hadoop.util.ProgramDriver to run
 * main methods of other classes, but first loads up default properties from a properties file.
 *
 * Borrows heavilly from MahoutDriver in the mahout project.
 *
 * for local running:
 *
 * $TT_HOME/bin/tt run shortJobName [over-ride ops]
 */
public class TamingTextDriver {

  private static final Logger log = LoggerFactory.getLogger(TamingTextDriver.class);

  private TamingTextDriver() {
  }

  public static void main(String[] args) throws Throwable {
    try {
      ProgramDriver programDriver = new ProgramDriver();
      Properties mainClasses = new Properties();
      InputStream propsStream = Thread.currentThread()
                                      .getContextClassLoader()
                                      .getResourceAsStream("driver.classes.props");

      mainClasses.load(propsStream);

      boolean foundShortName = false;
      for(Object key :  mainClasses.keySet()) {
        String keyString = (String) key;
        if(args.length > 0 && shortName(mainClasses.getProperty(keyString)).equals(args[0])) {
          foundShortName = true;
        }
        addClass(programDriver, keyString, mainClasses.getProperty(keyString));
      }
      if(args.length < 1 || args[0] == null || args[0].equals("-h") || args[0].equals("--help")) {
        programDriver.driver(args);
      }
      String progName = args[0];
      if(!foundShortName) {
        addClass(programDriver, progName, progName);
      }
      shift(args);

      InputStream defaultsStream = Thread.currentThread()
                                         .getContextClassLoader()
                                         .getResourceAsStream(progName + ".props");

      Properties mainProps = new Properties();
      if (defaultsStream != null) { // can't find props file, use empty props.
        mainProps.load(defaultsStream);
      } else {
        log.warn("No " + progName + ".props found on classpath, will use command-line arguments only");
      }
      Map<String,String[]> argMap = new HashMap<String,String[]>();
      int i=0;
      while(i<args.length && args[i] != null) {
        List<String> argValues = new ArrayList<String>();
        String arg = args[i];
        i++;
        if(arg.length() > 2 && arg.charAt(1) == 'D') { // '-Dkey=value' or '-Dkey=value1,value2,etc' case
          String[] argSplit = arg.split("=");
          arg = argSplit[0];
          if(argSplit.length == 2) {
            argValues.add(argSplit[1]);
          }
        } else {                                      // '-key [values]' or '--key [values]' case.
          while(i<args.length && args[i] != null) {
            if(args[i].length() > 0 && args[i].charAt(0) != '-') {
              argValues.add(args[i]);
              i++;
            } else {
              break;
            }
          }
        }
        argMap.put(arg, argValues.toArray(new String[argValues.size()]));
      }
      for (String key : mainProps.stringPropertyNames()) {
        String[] argNamePair = key.split("\\|");
        String shortArg = '-' + argNamePair[0].trim();
        String longArg = argNamePair.length < 2 ? null : "--" + argNamePair[1].trim();
        if(!argMap.containsKey(shortArg) && (longArg == null || !argMap.containsKey(longArg))) {
          argMap.put(longArg, new String[] { mainProps.getProperty(key) } );
        }
      }
      List<String> argsList = new ArrayList<String>();
      argsList.add(progName);
      for(String arg : argMap.keySet()) {
        if(arg.startsWith("-D")) { // arg is -Dkey - if value for this !isEmpty(), then arg -> -Dkey + "=" + value
          if(argMap.get(arg).length > 0 && !argMap.get(arg)[0].trim().isEmpty()) {
            arg += '=' + argMap.get(arg)[0].trim();
          }
        }
        argsList.add(arg);
        if(!arg.startsWith("-D")) {
          argsList.addAll(Arrays.asList(argMap.get(arg)));
        }
      }
      programDriver.driver(argsList.toArray(new String[argsList.size()]));
    } catch (Throwable e) {
      log.error("TamingTextDriver failed with args: " + Arrays.toString(args) + '\n' + e.getMessage());
      throw e;
    }
  }

  private static String[] shift(String[] args) {
    System.arraycopy(args, 1, args, 0, args.length - 1);
    args[args.length - 1] = null;
    return args;
  }

  private static String shortName(String valueString) {
    if (valueString.contains(":")) {
      return valueString.substring(0, valueString.indexOf(':')).trim();
    } else {
      return valueString;
    }
  }

  private static String desc(String valueString) {
    if (valueString.contains(":")) {
      return valueString.substring(valueString.indexOf(':')).trim();
    } else {
      return valueString;
    }
  }

  private static void addClass(ProgramDriver driver, String classString, String descString) {
    try {
      Class<?> clazz = Class.forName(classString);
      driver.addClass(shortName(descString), clazz, desc(descString));
    } catch (ClassNotFoundException e) {
      log.warn("Unable to add class: " + classString, e);
    } catch (Throwable t) {
      log.warn("Unable to add class: " + classString, t);
    }
  }

}
