# Lists

For `RecyclerView`, Ema provides adapters based on `ListAdapter` and ViewBinding.

## One item type

```kotlin
class HomeSingleAdapter : EmaRecyclerAdapter<HomeLayoutItemUserBinding, User>() {

    override fun createViewBinding(inflater: LayoutInflater, container: ViewGroup?) =
        HomeLayoutItemUserBinding.inflate(inflater, container, false)

    override fun HomeLayoutItemUserBinding.bind(
        item: User,
        viewType: Int,
        holder: EmaViewHolder<User>,
        payloads: MutableList<Any>
    ) {
        tvHomeItemUserAvatar.text = item.initials
        tvHomeItemUser.text = item.fullName
    }
}
```

## Several item types

Extend `EmaMultiRecyclerAdapter`, return the type from `getItemViewType` and create the binding for each type:

```kotlin
class HomeMultiAdapter : EmaMultiRecyclerAdapter<User>() {

    override fun getItemViewType(position: Int) = currentList[position].role.ordinal

    override fun createMultiViewHolder(view: ViewGroup, viewType: Int): EmaAdapterMultiViewHolder {
        val inflater = LayoutInflater.from(view.context)
        val binding = when (Role.entries[viewType]) {
            Role.ADMIN -> HomeLayoutItemAdminBinding.inflate(inflater, view, false)
            Role.BASIC -> HomeLayoutItemUserBinding.inflate(inflater, view, false)
        }
        return EmaAdapterMultiViewHolder(binding, viewType)
    }

    override fun ViewBinding.bind(item: User, viewType: Int, holder: EmaViewHolder<User>, payloads: MutableList<Any>) {
        when (Role.entries[viewType]) {
            Role.ADMIN -> (this as HomeLayoutItemAdminBinding).apply { /* ... */ }
            Role.BASIC -> (this as HomeLayoutItemUserBinding).apply { /* ... */ }
        }
    }
}
```

## Feeding the adapter

Feed it from the state, with `submitList`, and let `DiffUtil` animate the changes:

```kotlin
bindForUpdate(state::userList) {
    adapter.submitList(it)
}
```

By default two items are the same if they are `equal`. Pass your own `DiffUtil.ItemCallback` to the constructor to compare by id,
or use `EmaBaseRecyclerAdapter.getAlwaysUpdateCallback()` to always redraw.

## Clicks

```kotlin
adapter.setOnItemClickListener { index, item ->
    viewModel.dispatch(HomeAction.UserClicked(item))
}
```

Send the click as an action. Do not change the list in the adapter: change the state and submit the new list.

## Decorations

`EmaVerticalSpaceDecoration`, `EmaGridSpacingItemDecoration` and `EmaSwipeToDeleteCallback` are available for spacing and swipe-to-delete.
