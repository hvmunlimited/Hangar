/*
 * Copyright © 2014 Jeff Corcoran
 *
 * This file is part of Hangar.
 *
 * Hangar is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Hangar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Hangar.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package ca.mimic.apphangar;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

public class IconHelper {

    protected IconCacheHelper ich;
    protected Context mContext;

    IconHelper (Context context) {
        mContext = context;
    }

    protected synchronized Bitmap cachedIconHelper(ImageView taskIcon, ComponentName componentTask, String taskName) {
        Drawable iconPackIcon = null;
        String cachedIconString = IconCacheHelper.getPreloadedIconUri(mContext, componentTask);

        if (cachedIconString == null) {
            if (ich == null) {
                ich = new IconCacheHelper(mContext);
                Tools.HangarLog("Loading new IconCacheHelper instance");
            }
            Intent intent = new Intent();
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setAction(Intent.ACTION_MAIN);
            intent.setPackage(componentTask.getPackageName());
            intent.setComponent(new ComponentName(componentTask.getPackageName(), componentTask.getClassName()));
            ResolveInfo rInfo = mContext.getPackageManager().resolveActivity(intent, 0);
            iconPackIcon = ich.getFullResIcon(rInfo);
            Tools.HangarLog("Caching bitmap for: " + taskName);
            cachedIconString = IconCacheHelper.preloadIcon(mContext, componentTask, Tools.drawableToBitmap(iconPackIcon));
            // taskIcon.setImageDrawable(iconPackIcon);
        } else {
            Tools.HangarLog("Using cached bitmap for: " + taskName);
        }
        if (taskIcon != null) {
            taskIcon.setImageURI(Uri.parse(cachedIconString));
            return null;
        } else {
            if (iconPackIcon == null) {
                return IconCacheHelper.getPreloadedIcon(mContext, componentTask);
            } else {
                return Tools.drawableToBitmap(iconPackIcon);
            }
        }
    }

    protected Bitmap cachedIconHelper(ComponentName componentTask, String taskName) {
        return cachedIconHelper(null, componentTask, taskName);
    }
}
