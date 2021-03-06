/*
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * MyCoRe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyCoRe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MyCoRe.  If not, see <http://www.gnu.org/licenses/>.
 */

/// <reference path="../definitions/pdf.d.ts" />


namespace mycore.viewer.widgets.pdf {

    export class PDFStructureModel extends model.StructureModel {
        /**
        * The Structure of a PDFDocument
        * @param _rootChapter The Root Chapter of a PDF ///TODO: should be named after the PDF name
        * @param _imageList The list of all Rendered Pages of the PDF
        * @param _chapterToImageMap A map to resolve the first image of the Chapter
        * @param _imageToChapterMap A map to resolve the Chapter of a Image
        * @param refPageMap
        * @param idPdfPageMap
        */
        constructor( _rootChapter: mycore.viewer.model.StructureChapter,
            _imageList: Array<model.StructureImage>,
            _chapterToImageMap: MyCoReMap<string, model.StructureImage>,
            _imageToChapterMap: MyCoReMap<string, model.StructureChapter>,
            _imageHrefImageMap:MyCoReMap<string, model.StructureImage>,
            public refPageMap: MyCoReMap<string, PDFPageProxy> ) {
            super( _rootChapter, _imageList, _chapterToImageMap, _imageToChapterMap, _imageHrefImageMap, true );
        }

    }

}
