package search;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import misc.Utils;
import data.Article;
import data.ArticleList;

public abstract class SearchMerger {

	protected List<ArticleList> allArticleList;
	
	public ArticleList execute() {
		ArticleList mergedArticleList = new ArticleList();

		Map<String, Article> articlesMap = new HashMap<String, Article>();
		
		for (ArticleList articleList : this.allArticleList) {
			for (Article article : articleList) {
				
				String title = article.getTitle();
				Integer year = article.getYear();
				
				String key = Utils.simplify(title + " " + year);
				
				Article existingArticle = articlesMap.get(key);
				if (existingArticle == null) {
					// new article
					mergedArticleList.add(article);
				} else {
					// TODO: merge duplicate?
				}
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
