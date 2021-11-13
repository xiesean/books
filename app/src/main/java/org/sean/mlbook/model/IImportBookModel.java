//Copyright (c) 2017. 章钦豪. All rights reserved.
package org.sean.mlbook.model;

import org.sean.mlbook.bean.LocBookShelfBean;

import java.io.File;

import io.reactivex.Observable;

public interface IImportBookModel {

    Observable<LocBookShelfBean> importBook(File book);
}
