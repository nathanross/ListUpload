/*
 * Copyright (c) 2014 
 * Nathan Ross <nrossit2@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package io.github.nathanross.listupload;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ListUpload {

  public static void main(String[] args) {
    GroupsMigrationBackend.initializeClient();
    String messagefolder = "messages";
    File f = new File(messagefolder);
    ArrayList<File> files = new ArrayList<File>(Arrays.asList(f.listFiles()));
    Map<Date,File> filesByDate = new HashMap<Date,File>();
    ArrayList<Date> filedates = new ArrayList<Date>();
    BufferedReader reader = null;
    Pattern emaildatePattern = Pattern.compile("^.*for <.*>; ([0-9]{1,2} [a-zA-Z]{1,4} [0-9]{4} [0-9]{2}:[0-9]{2}:[0-9]{2}).*$");
    Pattern seconddatePattern = Pattern.compile("^ ?[dD]ate: [a-zA-Z]{1,4}, ([0-9]{1,2} [a-zA-Z]{1,4} [0-9]{4} [0-9]{2}:[0-9]{2}:[0-9]{2}).*$");
    Matcher m;
    
    for (int i=0;i<files.size();i++) {
      Date emaildate = null;

      try {
          reader = new BufferedReader(new FileReader(files.get(i)));
      
          String line;
          boolean wasmatch = false;
          while ((line = reader.readLine()) != null) {
              m = emaildatePattern.matcher(line);
              if (m.matches()) {
                //System.out.println(line);
                //System.out.println(m.group(1));
                try {
                emaildate = new SimpleDateFormat("d MMM yyyy kk:mm:ss",Locale.ENGLISH).parse(m.group(1));
                //System.out.println(emaildate);
                } catch (ParseException e) {
                  System.out.println("couldn't parse date.");
                  System.exit(-1);
                }
                wasmatch = true;
                break;
              }
          }
          reader.close();
          if (!wasmatch) {
            reader = new BufferedReader(new FileReader(files.get(i)));
            
            while ((line = reader.readLine()) != null) {
                m = seconddatePattern.matcher(line);
                if (m.matches()) {
                  //System.out.println(line);
                  //System.out.println(m.group(1));
                  try {
                  emaildate = new SimpleDateFormat("d MMM yyyy kk:mm:ss",Locale.ENGLISH).parse(m.group(1));
                  //System.out.println(emaildate);
                  } catch (ParseException e) {
                    System.out.println("couldn't parse date.");
                    System.exit(-1);
                  }
                  wasmatch = true;
                  break;
                }
            }
            if (!wasmatch) {
              System.out.println("no match:" + files.get(i).getName());
              System.exit(-1);
            }
          }
      
      } catch (IOException e) {
          e.printStackTrace();
      } finally {
          try {
              reader.close();
          } catch (IOException e) {
              e.printStackTrace();
          }
      }
      filedates.add(emaildate);
      filesByDate.put(emaildate, files.get(i));
    }
    Collections.sort(filedates);
    
    for (int i=0;i<files.size();i++) {
        //System.out.println(filedates.get(i)); 
        File nextf = filesByDate.get(filedates.get(i));
         System.out.println(Integer.toString(i) +
            "/" + Integer.toString(files.size())+":"+nextf.getName());
      
         GroupsMigrationBackend.sendEmail("yourgroup@yourdomain.org", nextf);
    } 
    //GroupsMigrationSample.sendEmailTest(args);

  }

}
