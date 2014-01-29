package kalpas.expensetracker.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;

public class Tags {

    // ******************************************************//

    private Tags() {
        throw new UnsupportedOperationException("use constructor with Context arg instaed");
    }

    private Tags(Context context) {
        this.context = context;
        this.transactionsDAO = new TransactionsDAO();
        buildCache();
    }

    private static volatile Tags instance;
    private final Context        context;

    public static Tags getInstance(Context context) {
        Tags result = instance;
        if (result == null) {
            synchronized (Tags.class) {
                if (instance == null) {
                    instance = new Tags(context);
                }
            }
            return instance;
        }

        return result;
    }

    // ******************************************************//

    private final static Splitter    splitter = Splitter.on(",").omitEmptyStrings().trimResults();

    private TransactionsDAO          transactionsDAO;

    private Multimap<String, String> cache;

    public void buildCache() {

        cache = LinkedListMultimap.create();
        List<Transaction> list = transactionsDAO.load(context);

        for (Transaction element : list) {
            String recipient = element.recipient;
            if (!Strings.isNullOrEmpty(recipient) && !Strings.isNullOrEmpty(element.tags)) {
                Iterable<String> tagArray = splitter.split(element.tags);
                cache.putAll(recipient, tagArray);
            }
        }
    }

    public List<String> getSuggestedTags(Transaction transaction) {
        List<String> result = new ArrayList<String>();
        if (transaction == null) {
            return result;
        }

        String recipient = transaction.recipient;
        if (!Strings.isNullOrEmpty(recipient) && cache.containsKey(recipient)) {
            Multiset<String> tags = Multisets.copyHighestCountFirst(HashMultiset.create(cache.get(recipient)));
            for (String tag : tags.elementSet()) {
                result.add(tag);
            }
        }
        return result;
    }

    public Collection<String> getTags() {
        List<String> result;
        HashSet<String> unsortedSet = new HashSet<String>();
        unsortedSet.addAll(cache.values());
        result = Lists.newArrayList(unsortedSet);
        Collections.sort(result, String.CASE_INSENSITIVE_ORDER);
        return result;
    }

    public Collection<String> getPopularTags() {
        LinkedList<String> result = new LinkedList<String>();
        Multiset<String> tags = Multisets.copyHighestCountFirst(HashMultiset.create(cache.values()));
        Iterator<String> iterator = tags.elementSet().iterator();
        while (iterator.hasNext() && result.size() < 6) {
            result.add(iterator.next());
        }
        return result;
    }

    @Deprecated
    public String debugOutput() {
        buildCache();

        StringBuilder builder = new StringBuilder();

        Multiset<String> keys = Multisets.copyHighestCountFirst(cache.keys());

        for (String key : keys.elementSet()) {
            int recipientCount = keys.count(key);
            builder.append(String.format("\"%s\" (%d):%n", key, recipientCount));
            Multiset<String> tags = Multisets.copyHighestCountFirst(HashMultiset.create(cache.get(key)));
            for (String tag : tags.elementSet()) {
                int count = tags.count(tag);
                builder.append(String.format("\t %s (%d - %.2f%%)%n", tag, count, count * 100. / recipientCount));
            }
            builder.append("_____________________\n");
        }

        return builder.toString();
    }
}
