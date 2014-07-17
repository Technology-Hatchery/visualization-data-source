/**********************************************************************
Copyright (c) 2009 Google Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
**********************************************************************/
package com.google.appengine.datanucleus.mapping;

import com.google.appengine.api.datastore.Key;

//Used in DataNucleus Core 2.2.5 API.  Current version is 4.0.0 (July 16, 2014)
//import org.datanucleus.store.mapped.mapping.SerialisedMapping;
import org.datanucleus.store.rdbms.mapping.java.SerialisedMapping;


/**
 * Custom mapping class for {@link Key}.  This lets us have
 * relations involving pojos where the pk is a {@link Key}.
 *
 * @author Max Ross <maxr@google.com>
 */
public class KeyMapping extends SerialisedMapping {

  @Override
  public Class getJavaType() {
    return Key.class;
  }
}
