/*
 * Copyright (c) 2010-2015 Pivotal Software, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License. See accompanying
 * LICENSE file.
 */


package com.gemstone.gemfire.cache.query.internal.parse;

import antlr.*;
import antlr.collections.*;
import com.gemstone.gemfire.cache.query.internal.QCompiler;

public class ASTImport extends GemFireAST {  
  private static final long serialVersionUID = 6002078657881181949L;
  
  public ASTImport() { }
  
  
  public ASTImport(Token t) {
    super(t);
  }
  
  
  @Override
  public void compile(QCompiler compiler) {
    StringBuffer nameBuf = new StringBuffer();
    String asName = null;
    AST child = getFirstChild();
    while (child != null) {
      if (child.getType() == OQLLexerTokenTypes.LITERAL_as) {
        child = child.getNextSibling();
        asName = child.getText();
        break;
      }
      else {
        nameBuf.append(child.getText());
        child = child.getNextSibling();
        if (child != null && child.getType() != OQLLexerTokenTypes.LITERAL_as) {
          nameBuf.append('.');
        }
      }
    }
    compiler.importName(nameBuf.toString(), asName);
  }
}
