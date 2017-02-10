package com.algolia.instantsearch.ui.views.filters;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.AttributeSet;

import com.algolia.instantsearch.R;
import com.algolia.instantsearch.helpers.Searcher;
import com.algolia.instantsearch.model.SearchResults;
import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Query;

/**
 * A widget that toggles between refining and not refining an attribute with a given value.
 */
public abstract class Toggle extends AppCompatCheckBox implements AlgoliaFacetFilter {
    /** The attribute to refine on. */
    public String attributeName;
    /** True if the OneValueToggle should hide when results are empty. */
    public boolean autoHide;
    /** A template to use as the OneValueToggle's text. */
    public String template;

    protected Searcher searcher;
    protected boolean shouldHide;

    public Toggle(Context context, AttributeSet attrs) {
        super(context, attrs);
        final TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.Toggle, 0, 0);
        try {
            attributeName = styledAttributes.getString(R.styleable.Toggle_attributeName);
            template = styledAttributes.getString(R.styleable.Toggle_template);
            autoHide = styledAttributes.getBoolean(R.styleable.Toggle_autoHide, false);
        } finally {
            styledAttributes.recycle();
        }
    }

    @NonNull @Override public final String getAttribute() {
        return attributeName;
    }

    @Override public final void initWithSearcher(@NonNull final Searcher searcher) {
        this.searcher = searcher;

        // Setup user interaction listener
        setOnCheckedChangeListener(getOnCheckedChangeListener());

        // First Search to fill template, eventually applying valueOff refinement
        searcher.search();
    }

    @Override public final void onReset() {
    }

    @Override public final void onResults(SearchResults results, boolean isLoadingMore) {
        checkShouldHide(results.nbHits == 0);
        if (template != null) {
            setText(applyTemplate(results));
        }
    }

    @Override public final void onError(Query query, AlgoliaException error) {
        checkShouldHide(true);
    }

    protected final void checkShouldHide(boolean newHideValue) {
        this.shouldHide = newHideValue;
        checkShouldHide();
    }

    protected final void checkShouldHide() {
        if (autoHide) {
            setVisibility(shouldHide ? GONE : VISIBLE);
        }
    }

    /**
     * Change the Toggle's attribute, updating facet refinements accordingly.
     *
     * @param newName the attribute's new name.
     */
    public final void setAttributeName(String newName) {
        searcher.removeFacet(attributeName).addFacet(newName);
        updateRefinementWithNewName(newName);
        attributeName = newName;
    }

    /**
     * Change the Toggle's autoHide setting, hiding it if needed.
     *
     * @param autoHide {@code true} if the Toggle should hide on empty results.
     */
    public final void setAutoHide(boolean autoHide) {
        this.autoHide = autoHide;
        checkShouldHide();
    }

    protected abstract OnCheckedChangeListener getOnCheckedChangeListener();

    protected abstract String applyTemplate(SearchResults results);

    protected abstract void updateRefinementWithNewName(String newName);
}