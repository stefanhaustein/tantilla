package org.kobjects.graphics;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.vanniktech.emoji.EmojiRange;
import com.vanniktech.emoji.EmojiUtils;
import com.vanniktech.emoji.emoji.Emoji;

import java.util.List;

public class Emojis {


  public static Drawable getDrawable(Context context, String codepoint) {

    List<EmojiRange> emojis = EmojiUtils.emojis(codepoint);
    if (emojis.size() == 0) {
      return null;
    }
    Emoji emoji = emojis.get(0).emoji;
    return emoji.getDrawable(context);
  }
}
