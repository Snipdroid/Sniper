package ren.imyan.sniper

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import ren.imyan.sniper.entity.IconInfo

class FlowPagingSource(private val data: Flow<List<IconInfo>>) : PagingSource<Int,IconInfo>(){
    override fun getRefreshKey(state: PagingState<Int, IconInfo>): Int? {
        return state.anchorPosition
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, IconInfo> {
       return try{
            val data = data.first()
            val nextPageNum = params.key ?: 1
            val fromIndex = (nextPageNum - 1) * params.loadSize
            val toIndex = nextPageNum * params.loadSize
            val pageData = data.subList(fromIndex, toIndex)
            LoadResult.Page(
                data = pageData,
                prevKey = if (nextPageNum == 1) null else nextPageNum - 1,
                nextKey = if (pageData.isEmpty()) null else nextPageNum + 1
            )
        }catch (e:Exception){
            LoadResult.Error(e)
        }
    }
}