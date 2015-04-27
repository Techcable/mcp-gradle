package jd_5.nailed.mcp;

import java.io.File;
import java.io.StringReader;
import java.net.URLClassLoader
import java.nio.charset.Charset
import java.util.*;

import au.com.bytecode.opencsv.CSVParser
import au.com.bytecode.opencsv.CSVReader

import com.google.common.collect.ImmutableList
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;
import com.google.common.io.Charsets;
import com.google.common.io.Files;

public class Utils {
    private Utils() {}
    
    public static CSVReader newCsvReader(File file) {
        return new CSVReader(Files.newReader(file, Charsets.UTF_8), CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER, CSVParser.DEFAULT_ESCAPE_CHARACTER, 1, false);
    }
    
    public static List<String> getClassPath() {
        URL[] urls = ((URLClassLoader)getClass().getClassLoader()).getURLs();
        List<String> result = Lists.newArrayListWithCapacity(urls.length);
        for (URL url : urls) {
            result.add(url.getPath());
        }
        return result;
    }
    
    public static ImmutableList<String> lines(String s) {
        try {
            return ImmutableList.copyOf(CharStreams.readLines(new StringReader(s)));
        } catch (Throwable t) {
            return ImmutableList.of();
        }
    }
    
    public static class DeletableIterator<T> extends Iterator<T> {
        public DeletableIterator(Iterator<T> it) {
            this.it = it;
        }
        
        private final Iterator<T> it;
        private final Set<T> deleted = new HashSet<>();
        private T nextElement = getNext();
        
        private T getNext() {
            if (it.hasNext()) {
                T n = it.next();
                if (deleted.contains(n) {
                    return getNext()
                } else {
                    return n;
                }
            } else {
                return null;
            }
        }
        
        @Override
        public T next() {
            T result = nextElement;
            nextElement = getNext();
            return result;
        }
        public void hasNext() {
            return nextElement != null;
        }
        public DeletableIterator<T> delete(T x) {
            deleted.add(x);
            return this;
        }
    }
}