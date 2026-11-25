import os
import zipfile
import xml.sax.saxutils

def xml_escape(text):
    if text is None:
        return ""
    return xml.sax.saxutils.escape(str(text))

class DocxBuilder:
    def __init__(self, filename):
        self.filename = filename
        self.paragraphs = []

    def add_title(self, text, subtitle=None):
        p = f'''<w:p>
          <w:pPr>
            <w:jc w:val="center"/>
            <w:spacing w:before="720" w:after="200"/>
          </w:pPr>
          <w:r>
            <w:rPr>
              <w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/>
              <w:b/>
              <w:sz w:val="50"/>
              <w:color w:val="0F172A"/>
            </w:rPr>
            <w:t>{xml_escape(text)}</w:t>
          </w:r>
        </w:p>'''
        self.paragraphs.append(p)
        if subtitle:
            sub = f'''<w:p>
              <w:pPr>
                <w:jc w:val="center"/>
                <w:spacing w:before="0" w:after="480"/>
              </w:pPr>
              <w:r>
                <w:rPr>
                  <w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/>
                  <w:i/>
                  <w:sz w:val="26"/>
                  <w:color w:val="475569"/>
                </w:rPr>
                <w:t>{xml_escape(subtitle)}</w:t>
              </w:r>
            </w:p>'''
            self.paragraphs.append(sub)

    def add_h1(self, text):
        p = f'''<w:p>
          <w:pPr>
            <w:pStyle w:val="Heading1"/>
            <w:spacing w:before="400" w:after="160"/>
            <w:pBdr>
              <w:bottom w:val="single" w:sz="12" w:space="8" w:color="2563EB"/>
            </w:pBdr>
          </w:pPr>
          <w:r>
            <w:rPr>
              <w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/>
              <w:b/>
              <w:sz w:val="34"/>
              <w:color w:val="1E3A8A"/>
            </w:rPr>
            <w:t>{xml_escape(text)}</w:t>
          </w:r>
        </w:p>'''
        self.paragraphs.append(p)

    def add_h2(self, text):
        p = f'''<w:p>
          <w:pPr>
            <w:pStyle w:val="Heading2"/>
            <w:spacing w:before="300" w:after="120"/>
          </w:pPr>
          <w:r>
            <w:rPr>
              <w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/>
              <w:b/>
              <w:sz w:val="28"/>
              <w:color w:val="2563EB"/>
            </w:rPr>
            <w:t>{xml_escape(text)}</w:t>
          </w:r>
        </w:p>'''
        self.paragraphs.append(p)

    def add_h3(self, text):
        p = f'''<w:p>
          <w:pPr>
            <w:pStyle w:val="Heading3"/>
            <w:spacing w:before="200" w:after="80"/>
          </w:pPr>
          <w:r>
            <w:rPr>
              <w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/>
              <w:b/>
              <w:sz w:val="24"/>
              <w:color w:val="334155"/>
            </w:rPr>
            <w:t>{xml_escape(text)}</w:t>
          </w:r>
        </w:p>'''
        self.paragraphs.append(p)

    def add_paragraph(self, text, bold_prefix=None):
        runs = []
        if bold_prefix:
            runs.append(f'''<w:r>
              <w:rPr>
                <w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/>
                <w:b/>
                <w:sz w:val="22"/>
                <w:color w:val="0F172A"/>
              </w:rPr>
              <w:t xml:space="preserve">{xml_escape(bold_prefix)} </w:t>
            </w:r>''')
        runs.append(f'''<w:r>
          <w:rPr>
            <w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/>
            <w:sz w:val="22"/>
            <w:color w:val="1E293B"/>
          </w:rPr>
          <w:t xml:space="preserve">{xml_escape(text)}</w:t>
        </w:r>''')
        p = f'''<w:p>
          <w:pPr>
            <w:spacing w:before="60" w:after="100" w:line="280" w:lineRule="auto"/>
          </w:pPr>
          {"".join(runs)}
        </w:p>'''
        self.paragraphs.append(p)

    def add_bullet(self, text, bold_prefix=None):
        runs = []
        if bold_prefix:
            runs.append(f'''<w:r>
              <w:rPr>
                <w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/>
                <w:b/>
                <w:sz w:val="22"/>
                <w:color w:val="0F172A"/>
              </w:rPr>
              <w:t xml:space="preserve">{xml_escape(bold_prefix)} </w:t>
            </w:r>''')
        runs.append(f'''<w:r>
          <w:rPr>
            <w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/>
            <w:sz w:val="22"/>
            <w:color w:val="1E293B"/>
          </w:rPr>
          <w:t xml:space="preserve">{xml_escape(text)}</w:t>
        </w:r>''')
        p = f'''<w:p>
          <w:pPr>
            <w:ind w:left="400" w:hanging="200"/>
            <w:spacing w:before="40" w:after="60"/>
          </w:pPr>
          <w:r>
            <w:rPr>
              <w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/>
              <w:b/>
              <w:color w:val="2563EB"/>
            </w:rPr>
            <w:t xml:space="preserve">• </w:t>
          </w:r>
          {"".join(runs)}
        </w:p>'''
        self.paragraphs.append(p)

    def add_callout(self, title, text):
        p = f'''<w:p>
          <w:pPr>
            <w:pBdr>
              <w:left w:val="single" w:sz="24" w:space="12" w:color="2563EB"/>
            </w:pBdr>
            <w:shd w:val="clear" w:color="auto" w:fill="F0F7FF"/>
            <w:spacing w:before="140" w:after="140"/>
            <w:ind w:left="240" w:right="240"/>
          </w:pPr>
          <w:r>
            <w:rPr>
              <w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/>
              <w:b/>
              <w:sz w:val="21"/>
              <w:color w:val="1E40AF"/>
            </w:rPr>
            <w:t xml:space="preserve">💡 {xml_escape(title)}: </w:t>
          </w:r>
          <w:r>
            <w:rPr>
              <w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/>
              <w:sz w:val="21"/>
              <w:color w:val="1E293B"/>
            </w:rPr>
            <w:t xml:space="preserve">{xml_escape(text)}</w:t>
          </w:r>
        </w:p>'''
        self.paragraphs.append(p)

    def add_code_block(self, code):
        lines = code.split("\n")
        for idx, line in enumerate(lines):
            bdr_top = ' w:val="single" w:sz="6" w:color="CBD5E1"' if idx == 0 else ' w:val="none"'
            bdr_bot = ' w:val="single" w:sz="6" w:color="CBD5E1"' if idx == len(lines) - 1 else ' w:val="none"'
            p = f'''<w:p>
              <w:pPr>
                <w:pBdr>
                  <w:top{bdr_top}/>
                  <w:left w:val="single" w:sz="18" w:space="8" w:color="2563EB"/>
                  <w:bottom{bdr_bot}/>
                  <w:right w:val="single" w:sz="6" w:color="CBD5E1"/>
                </w:pBdr>
                <w:shd w:val="clear" w:color="auto" w:fill="F8FAFC"/>
                <w:spacing w:before="0" w:after="0" w:line="240" w:lineRule="auto"/>
                <w:ind w:left="200" w:right="200"/>
              </w:pPr>
              <w:r>
                <w:rPr>
                  <w:rFonts w:ascii="Consolas" w:hAnsi="Consolas"/>
                  <w:sz w:val="17"/>
                  <w:color w:val="0F172A"/>
                </w:rPr>
                <w:t xml:space="preserve">{xml_escape(line) if line else " "}</w:t>
              </w:r>
            </w:p>'''
            self.paragraphs.append(p)
        self.paragraphs.append('<w:p><w:pPr><w:spacing w:before="40" w:after="80"/></w:pPr></w:p>')

    def add_table(self, headers, rows):
        tbl_xml = ['<w:tbl>',
                   '<w:tblPr>',
                   '  <w:tblW w:w="5000" w:type="pct"/>',
                   '  <w:tblBorders>',
                   '    <w:top w:val="single" w:sz="6" w:space="0" w:color="CBD5E1"/>',
                   '    <w:bottom w:val="single" w:sz="6" w:space="0" w:color="CBD5E1"/>',
                   '    <w:insideH w:val="single" w:sz="4" w:space="0" w:color="E2E8F0"/>',
                   '    <w:insideV w:val="none"/>',
                   '    <w:left w:val="none"/>',
                   '    <w:right w:val="none"/>',
                   '  </w:tblBorders>',
                   '</w:tblPr>']
        tbl_xml.append('<w:tr><w:trPr><w:tblHeader/></w:trPr>')
        for h in headers:
            tbl_xml.append(f'''<w:tc>
              <w:tcPr>
                <w:shd w:val="clear" w:color="auto" w:fill="1E3A8A"/>
                <w:tcMar><w:top w:w="120" w:type="dxa"/><w:bottom w:w="120" w:type="dxa"/><w:left w:w="140" w:type="dxa"/><w:right w:w="140" w:type="dxa"/></w:tcMar>
              </w:tcPr>
              <w:p>
                <w:pPr><w:spacing w:before="40" w:after="40"/></w:pPr>
                <w:r>
                  <w:rPr>
                    <w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/>
                    <w:b/>
                    <w:sz w:val="20"/>
                    <w:color w:val="FFFFFF"/>
                  </w:rPr>
                  <w:t>{xml_escape(h)}</w:t>
                </w:r>
              </w:p>
            </w:tc>''')
        tbl_xml.append('</w:tr>')

        for r_idx, row in enumerate(rows):
            fill = "F8FAFC" if r_idx % 2 == 1 else "FFFFFF"
            tbl_xml.append('<w:tr>')
            for c_idx, cell in enumerate(row):
                is_first_col = (c_idx == 0)
                font = "Consolas" if is_first_col else "Calibri"
                bold = "<w:b/>" if is_first_col else ""
                color = "0F172A" if is_first_col else "334155"
                tbl_xml.append(f'''<w:tc>
                  <w:tcPr>
                    <w:shd w:val="clear" w:color="auto" w:fill="{fill}"/>
                    <w:tcMar><w:top w:w="100" w:type="dxa"/><w:bottom w:w="100" w:type="dxa"/><w:left w:w="140" w:type="dxa"/><w:right w:w="140" w:type="dxa"/></w:tcMar>
                  </w:tcPr>
                  <w:p>
                    <w:pPr><w:spacing w:before="40" w:after="40" w:line="240" w:lineRule="auto"/></w:pPr>
                    <w:r>
                      <w:rPr>
                        <w:rFonts w:ascii="{font}" w:hAnsi="{font}"/>
                        {bold}
                        <w:sz w:val="19"/>
                        <w:color w:val="{color}"/>
                      </w:rPr>
                      <w:t xml:space="preserve">{xml_escape(cell)}</w:t>
                    </w:r>
                  </w:p>
                </w:tc>''')
            tbl_xml.append('</w:tr>')
        tbl_xml.append('</w:tbl>')
        tbl_xml.append('<w:p><w:pPr><w:spacing w:before="60" w:after="100"/></w:pPr></w:p>')
        self.paragraphs.append("\n".join(tbl_xml))

    def save(self):
        body_xml = "".join(self.paragraphs)
        doc_xml = f'''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
            xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <w:body>
    {body_xml}
    <w:sectPr>
      <w:pgSz w:w="12240" w:h="15840"/>
      <w:pgMar w:top="1440" w:right="1440" w:bottom="1440" w:left="1440" w:header="720" w:footer="720" w:gutter="0"/>
    </w:sectPr>
  </w:body>
</w:document>'''

        content_types = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
</Types>"""

        rels = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
</Relationships>"""

        with zipfile.ZipFile(self.filename, "w", zipfile.ZIP_DEFLATED) as z:
            z.writestr("[Content_Types].xml", content_types)
            z.writestr("_rels/.rels", rels)
            z.writestr("word/document.xml", doc_xml)
        print(f"Saved docx to {self.filename} ({os.path.getsize(self.filename)} bytes)")
