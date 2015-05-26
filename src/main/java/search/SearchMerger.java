package search;

import java.util.List;

import data.Article;
import data.ArticleList;

public abstract class SearchMerger {

	protected List<ArticleList> allArticleList;
	
	public ArticleList execute() {
		// TODO: merge duplicates
		
		ArticleList mergedArticleList = new ArticleList();
		
		for (ArticleList articleList : this.allArticleList) {
			for (Article article : articleList) {
				// STUB: add everything
				mergedArticleList.add(article);
			}
		}

		return mergedArticleList;
	}

	public List<ArticleList> getAllArticleList() {
		return this.allArticleList;
	}

	public void setAllArticleList(List<ArticleList> allArticleList) {
		this.allArticleList = allArticleList;
	}
	
}
