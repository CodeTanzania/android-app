package com.github.codetanzania.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.github.codetanzania.Constants;
import com.github.codetanzania.api.model.Open311Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utils for API Services. Created by kmoze on 5/30/17.
 */

public class Open311ServicesUtil {

    private static final String KEY_OPEN311_CACHE = "open311.cache";

    private static Open311Service getByIndex(Collection<Open311Service> collection, String index) {
        for (Open311Service aService: collection) {
            if (index.equals(aService.id)) {
                return aService;
            }
        }
        return null;
    }

    /**
     * Cache data to improve performance. With this approach, the application
     * should request new data in the background (e.g. using a service).
     *
     * Internally, data are cached as csv, without headers
     *
     * @param context - android context helps the utility to access resources.
     * @param open311Services - a list of services we need to cache
     */
    public static void cache(@NonNull Context context, @NonNull List<Open311Service> open311Services) {
        SharedPreferences prefs = context
                .getSharedPreferences(Constants.Const.KEY_SHARED_PREFS, Context.MODE_PRIVATE);
        Set<Open311Service> merger = new HashSet<>(cached(context));
        // for each service in a newer list
        for (Open311Service newerService: open311Services) {
            // if data already exists in an old cache:
            // remove it (newer item gets preference)
            Open311Service oldService = getByIndex(merger, newerService.id);
            if (oldService != null) {
                merger.remove(oldService);
            } // end if
            // add newer item
            merger.add(newerService);
        }
        // for each openCamera-311-service do:
        Set<String> aSet = new HashSet<>(open311Services.size());
        for (Open311Service aService: merger) {
            // index each service by serializing it
            String data = String.format("%s,%s,%s,%s,%s", aService.id,
                    aService.name, aService.code, aService.color, aService.description);
            aSet.add(data);
        }
        // end for
        prefs.edit().putStringSet(KEY_OPEN311_CACHE, aSet).apply();
    }

    /**
     * Retrieves cached data from the shared preference.
     * @param context - android context helps the utility to access resources.
     */
    public static List<Open311Service> cached(@NonNull Context context) {
        SharedPreferences prefs = context
                .getSharedPreferences(Constants.Const.KEY_SHARED_PREFS, Context.MODE_PRIVATE);
        Set<String> aSet = prefs.getStringSet(KEY_OPEN311_CACHE, null);
        if (aSet == null) {
            return Collections.emptyList();
        }
        List<Open311Service> list = new ArrayList<>(aSet.size());
        // for each line in a set do:
        for (String aLine: aSet) {
            String parts[] = aLine.split(",");
            // did we add previously?
            Open311Service service = new Open311Service();
            service.id = parts[0];
            service.name = parts[1];
            service.code = parts[2];
            service.color = parts[3];
            service.description = parts[4];

            // add service
            list.add(service);
        } // end for
        // return data
        return list;
    }
}
