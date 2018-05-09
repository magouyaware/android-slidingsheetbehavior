# SlidingSheetBehavior
_A supercharged port/fork of Android's BottomsheetBehavior, with additional features like sliding from any edge,  edge tracking, and ViewPager support._

### Background _(Why SlidingSheetBehavior)_
When Google introduced layout behaviors, they seemed really cool, but also a bit complex.  Thankfully, they created a few layout behaviors 
that could be used out of the box, one of which was [BottomSheetBehavior](https://developer.android.com/reference/android/support/design/widget/BottomSheetBehavior) (see [source](https://android.googlesource.com/platform/frameworks/support.git/+/master/design/src/android/support/design/widget/BottomSheetBehavior.java)).  I started using it and it was... _almost great._
<br/>
It had a few issues and limitations:
- It only worked from the bottom edge _(hence the name)_
- It didn't support edge dragging (opening the sheet when fully hidden)
- The calculated values for the auto peek size seemed... _bizarre._ Too large in portrait and too small in landscape
- It didn't work with ViewPagers.

I found a great tool ([ViewPagerBottomSheet](https://github.com/laenger/ViewPagerBottomSheet)) to fix one of the three problems, but I couldn't find anything to solve the other two. Yea, there is [DrawerLayout](https://developer.android.com/reference/android/support/v4/widget/DrawerLayout), but it has it's own limitations and may not be exactly what you want all the time.

An idea formed in my mind, and I devised a plan to help me learn more about layout behaviors _and_ solve my BottomSheetBehavior woes...   SlidingSheetLayout was born.  I started with the [source](https://android.googlesource.com/platform/frameworks/support.git/+/master/design/src/android/support/design/widget/BottomSheetBehavior.java) for BottomSheetLayout, pulled in support for ViewPagers (props to [ViewPagerBottomSheet](https://github.com/laenger/ViewPagerBottomSheet) from [laenger's](https://github.com/laenger)), and added the additional features that I wanted/needed.

### Usage
See the example app source for full examples of how to use SlidingSheetBehavior, but it is _mostly_ (though not completely) a drop-in replacement for BottomSheetBehavior.  Get started in three easy steps:

1. Make sure you define your custom namespace in your layout file: 
    ```
    xmlns:sheet="http://schemas.android.com/apk/res-auto"
    ```
2. Create your layout as you normally would: 
    * The parent of your sheet must be a [ConstraintLayout](https://developer.android.com/reference/android/support/constraint/ConstraintLayout)
    * Tell your sheet that it is going to be a sliding sheet: `sheet:layout_behavior="@string/sliding_sheet_behavior"`
3. Set up additional properties for your sliding sheet:
    ```html
    sheet:behavior_hideable="true|false" <!-- Default: false --> 
    sheet:behavior_peekSize="100dp" <!-- Omit for auto peek size: Inverted 16:9 ratio of parent's height -->
    sheet:behavior_slideEdge="left|right|top|bottom" <!-- Default: bottom --> 
    sheet:behavior_enableEdgeDrag="true|false" <!-- Default: false --> 
    sheet:behavior_skipCollapsed="true|false" <!-- Default: false --> 
    ```

Sample (bare bones) Layout for two sliding sheets: 
```xml
<android.support.design.widget.CoordinatorLayout
      xmlns:android="http://schemas.android.com/apk/res/android"
      xmlns:sheet="http://schemas.android.com/apk/res-auto"
      android:layout_width="match_parent"
      android:layout_height="match_parent">
      <LinearLayout
          android:layout_width="200dp"
          android:layout_height="match_parent"
          sheet:layout_behavior="@string/sliding_sheet_behavior"
          sheet:behavior_hideable="true"
          sheet:behavior_peekSize="100dp"
          sheet:behavior_slideEdge="left"
          sheet:behavior_enableEdgeDrag="true">
      <FrameLayout
          android:layout_width="240dp"
          android:layout_height="match_parent"
          sheet:layout_behavior="@string/sliding_sheet_behavior"
          sheet:behavior_slideEdge="right">
</android.support.design.widget.CoordinatorLayout>    
```

### Next Steps
1. Create more robust sample app, to show various use cases
1. Need more thorough testing of sheets with nested scrolling views
1. Update Readme with gifs to show functionality
1. Publish library to jcenter
1. Create Xamarin bindings and nuget package (help would be appreciated)
