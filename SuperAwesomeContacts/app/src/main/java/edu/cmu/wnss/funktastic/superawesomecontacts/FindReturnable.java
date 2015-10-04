package edu.cmu.wnss.funktastic.superawesomecontacts;

/**
 * Created by Funky McAwesome on 9/3/15.
 *
 * Serves as a basic callback interface for classes which do not need the additional
 * overhead of the Handler class (ie, not updating UI from a non-UI thread)
 */
public interface FindReturnable {
    public void setSearchResult(String results);
}
