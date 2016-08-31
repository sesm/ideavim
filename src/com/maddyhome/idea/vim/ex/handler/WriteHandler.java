/*
 * IdeaVim - Vim emulator for IDEs based on the IntelliJ platform
 * Copyright (C) 2003-2016 The IdeaVim authors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.maddyhome.idea.vim.ex.handler;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.encoding.EncodingRegistry;
import com.maddyhome.idea.vim.VimPlugin;
import com.maddyhome.idea.vim.ex.CommandHandler;
import com.maddyhome.idea.vim.ex.CommandName;
import com.maddyhome.idea.vim.ex.ExCommand;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 *
 */
public class WriteHandler extends CommandHandler {
  public WriteHandler() {
    super(new CommandName[]{
      new CommandName("w", "rite")
    }, RANGE_OPTIONAL | ARGUMENT_OPTIONAL);
  }

  public boolean execute(@NotNull Editor editor, @NotNull DataContext context, @NotNull ExCommand cmd) {
    String arg = cmd.getArgument();
    final boolean forced;
    forced = arg.startsWith("!");
    if(forced) {
      arg = arg.substring(1).trim();
    }

    if(arg.length() == 0) {
      VimPlugin.getFile().saveFile(editor, context);
    } else {
      File out = new File(arg);
      if(out.isFile() && !forced) {
        new Notification(
          VimPlugin.IDEAVIM_STICKY_NOTIFICATION_ID,
          VimPlugin.IDEAVIM_NOTIFICATION_TITLE,
          "E13: file exists (add ! to override)",
          NotificationType.INFORMATION).notify(null);
      }
      try {
        EncodingRegistry encodingRegistry = EncodingRegistry.getInstance();
        VirtualFile file = FileDocumentManager.getInstance().getFile(editor.getDocument());
        Charset encoding = encodingRegistry.getEncoding(file, true);
        if(encoding == null) {
          encoding = encodingRegistry.getDefaultCharset();
        }
        PrintWriter writer = new PrintWriter(arg, encoding.name());
        String text = editor.getDocument().getText();
        writer.print(text);
        writer.close();
      }
      catch (FileNotFoundException e) {
        return false;
      }
      catch (UnsupportedEncodingException e) {
        return false;
      }
    }

    return true;

    // TODO:
    // - Save text fragment
    // - Tab completion (no UI, like in gVim)
    // - Handle file paths correctly relatively to current document root (gVim doesn't do this!)
    // - Don't ask for override if the file has already been written to in scope of this session
    //
    // Qs:
    // - What if the file we overwrite is the part of current project? Do we need to work with PSI, etc?
    // - UI component for errors: standard error balloon or custom error panel at bottom?


    // Idea: run terminal commands in terminal
  }
}
