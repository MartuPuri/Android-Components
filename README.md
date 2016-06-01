[![Download](https://api.bintray.com/packages/martupuri/Masacre-Android/android-components/images/download.svg) ](https://bintray.com/martupuri/Masacre-Android/android-components/_latestVersion)

# Android Components

Android components is a project which contains impromevents of native android components.

# Usage

## Using Gradle

You can just add the dependency like below

```
repositories {
    jcenter()
}
```

And then you can add the library as dependency

```
dependencies {
    compile 'com.masacre:android-components:1.0.0'
}
```

# Components

## ClickableRecyclerView

The ClickableRecyclerView improve the **RecyclerView** by adding method to handle the android.view.View.OnClickListener and android.view.View.OnLongClickListener of each android.support.v7.widget.RecyclerView.ViewHolder.

There are two exclusive ways of handle the click event.

1.  Register the following listeners **OnViewHolderClickListener** and/or **OnViewHolderLongClickListener** in the recycler view
2.  Override the methods android.view.View.OnClickListener#**onClick(View)** and/or android.view.View.OnLongClickListener#**onLongClick(View)** in the view holder

### Sample



```java

final ClickableRecyclerView recyclerView = (ClickableRecyclerView) findViewById(R.id.recycler_view);
recyclerView.setLayoutManager(new LinearLayoutManager(this));

final DCSeriesAdapter dcSeriesAdapter = new DCSeriesAdapter(this, Arrays.asList("Arrow", "The Flash", "DC's Legends of Tomorrow"));

recyclerView.setAdapter(dcSeriesAdapter);

```



The class `DCSeriesAdapter` must extends from `ClickableRecyclerView.ClickableAdapter<DCViewHolder>`, and `DCViewHolder` must extends from`ClickableRecyclerView.ClickableViewHolder`

In this example we are going to handle the **onClick** registering the listener in the recycler view and handle the **onLongClick** in the DCViewHolder.

When we register the **OnViewHolderClickListener** the method **onClick** implemented in the view holder will not be called.

```java
recyclerView.setOnViewHolderClickListener(new ClickableRecyclerView.OnViewHolderClickListener<DCSeriesViewHolder>() {
    @Override
    public void onItemClick(final DCSeriesViewHolder holder, final int position) {
        Toast.makeText(MainActivity.this, "Recycler onClick: " + holder.getText(), Toast.LENGTH_SHORT).show();
    }
});
```

Now we have to implement the **onLongClick** in the `DCSeriesViewHolder`


```java
public class DCSeriesViewHolder extends ClickableRecyclerView.ClickableViewHolder {

    private final Context context;
    private final TextView seriesTitle;

    public DummyViewHolder(final Context context, final View view) {
        super(view);
        this.context = context;
        this.seriesTitle = (TextView) view.findViewById(R.id.textview);
    }

    private void findViews(final View view) {
        this.seriesTitle = (TextView) view.findViewById(R.id.textview);
    }

    public void show(final String value) {
        seriesTitle.setText(value);
    }

    @Override
    public boolean onLongClick(View v) {
        Toast.makeText(context, "DCSeriesViewHolder onLongClick: " + textView.getText(), Toast.LENGTH_SHORT).show();
        return true;
    }

    public String getText() {
        return seriesTitle.getText().toString();
    }
}
```

# Versions

*	1.0.0 
    * ClickableRecyclerView

# Copyright and License
Copyright 2016 Martin Purita.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use
this work except in compliance with the License. You may obtain a copy of the
License at:

http://www.apache.org/licenses/LICENSE-2.0
