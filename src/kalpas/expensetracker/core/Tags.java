package kalpas.expensetracker.core;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import android.content.Context;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;

public class Tags {

    private Tags() {
    }

    private static volatile Tags instance;

    public static Tags getTagsProvider() {
        Tags result = instance;
        if (result == null) {
            synchronized (Tags.class) {
                if (instance == null) {
                    instance = new Tags();
                }
            }
            return instance;
        }

        return result;
    }

    private static final Splitter splitter       = Splitter.on(",").omitEmptyStrings().trimResults();
    private static final String   TAGS_FILE_NAME = "tags.json";
    private Gson                  gson           = new Gson();

    public List<String> getTags(Context context) {
        return Lists.newArrayList(load(context));
    }

    public void addTags(String tagString, Context context) {
        if (StringUtils.isEmpty(tagString)) {
            return;
        }
        Set<String> tags = load(context);
        Iterables.addAll(tags, splitter.split(tagString));
        save(tags, context);
    }

    private void save(Set<String> tags, Context context) {
        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(TAGS_FILE_NAME, Context.MODE_PRIVATE);
            fos.write(gson.toJson(tags.toArray(new String[tags.size()])).getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Set<String> load(Context context) {
        Set<String> set = new HashSet<String>();
        FileInputStream fis;
        try {
            fis = context.openFileInput(TAGS_FILE_NAME);
            set = Sets.newHashSet(gson.fromJson(new InputStreamReader(fis), String[].class));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return set;
    }

    public void deleteAll(Context context) {
        context.deleteFile(TAGS_FILE_NAME);
    }

}
