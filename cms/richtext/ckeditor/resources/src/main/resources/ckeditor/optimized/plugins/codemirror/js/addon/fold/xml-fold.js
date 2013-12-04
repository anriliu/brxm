﻿(function(){function h(a,c,d,b){this.line=c;this.ch=d;this.cm=a;this.text=a.getLine(c);this.min=b?b.from:a.firstLine();this.max=b?b.to-1:a.lastLine()}function j(a,c){var d=a.cm.getTokenTypeAt(i(a.line,c));return d&&/\btag\b/.test(d)}function o(a){if(!(a.line>=a.max))return a.ch=0,a.text=a.cm.getLine(++a.line),!0}function p(a){if(!(a.line<=a.min))return a.text=a.cm.getLine(--a.line),a.ch=a.text.length,!0}function l(a){for(;;){var c=a.text.indexOf(">",a.ch);if(-1==c)if(o(a))continue;else break;if(j(a,
c+1)){var d=a.text.lastIndexOf("/",c),d=-1<d&&!/\S/.test(a.text.slice(d+1,c));a.ch=c+1;return d?"selfClose":"regular"}a.ch=c+1}}function m(a){for(;;){var c=a.ch?a.text.lastIndexOf("<",a.ch-1):-1;if(-1==c)if(p(a))continue;else break;if(j(a,c+1)){k.lastIndex=c;a.ch=c;var d=k.exec(a.text);if(d&&d.index==c)return d}else a.ch=c}}function q(a){for(;;){k.lastIndex=a.ch;var c=k.exec(a.text);if(!c)if(o(a))continue;else break;if(j(a,c.index+1))return a.ch=c.index+c[0].length,c;a.ch=c.index+1}}function n(a,
c){for(var d=[];;){var b=q(a),e,f=a.line,g=a.ch-(b?b[0].length:0);if(!b||!(e=l(a)))break;if("selfClose"!=e)if(b[1]){for(var h=d.length-1;0<=h;--h)if(d[h]==b[2]){d.length=h;break}if(0>h&&(!c||c==b[2]))return{tag:b[2],from:i(f,g),to:i(a.line,a.ch)}}else d.push(b[2])}}function r(a,c){for(var d=[];;){var b;a:{for(b=a;;){var e=b.ch?b.text.lastIndexOf(">",b.ch-1):-1;if(-1==e)if(p(b))continue;else{b=void 0;break a}if(j(b,e+1)){var f=b.text.lastIndexOf("/",e),f=-1<f&&!/\S/.test(b.text.slice(f+1,e));b.ch=
e+1;b=f?"selfClose":"regular";break a}else b.ch=e}b=void 0}if(!b)break;if("selfClose"==b)m(a);else{b=a.line;e=a.ch;f=m(a);if(!f)break;if(f[1])d.push(f[2]);else{for(var g=d.length-1;0<=g;--g)if(d[g]==f[2]){d.length=g;break}if(0>g&&(!c||c==f[2]))return{tag:f[2],from:i(a.line,a.ch),to:i(b,e)}}}}}var i=CodeMirror.Pos,k=RegExp("<(/?)([A-Z_a-z\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\\u1FFF\\u200C-\\u200D\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD][A-Z_a-z\\u00C0-\\u00D6\\u00D8-\\u00F6\\u00F8-\\u02FF\\u0370-\\u037D\\u037F-\\u1FFF\\u200C-\\u200D\\u2070-\\u218F\\u2C00-\\u2FEF\\u3001-\\uD7FF\\uF900-\\uFDCF\\uFDF0-\\uFFFD-:.0-9\\u00B7\\u0300-\\u036F\\u203F-\\u2040]*)",
"g");CodeMirror.registerHelper("fold","xml",function(a,c){for(var d=new h(a,c.line,0);;){var b=q(d),e;if(!b||d.line!=c.line||!(e=l(d)))break;if(!b[1]&&"selfClose"!=e)return c=i(d.line,d.ch),(d=n(d,b[2]))&&{from:c,to:d.from}}});CodeMirror.tagRangeFinder=CodeMirror.fold.xml;CodeMirror.findMatchingTag=function(a,c,d){var b=new h(a,c.line,c.ch,d);if(!(-1==b.text.indexOf(">")&&-1==b.text.indexOf("<"))){var e=l(b),f=e&&i(b.line,b.ch),g=e&&m(b);if(e&&!("selfClose"==e||!g||0<(b.line-c.line||b.ch-c.ch))){c=
{from:i(b.line,b.ch),to:f,tag:g[2]};if(g[1])return{open:r(b,g[2]),close:c,at:"close"};b=new h(a,f.line,f.ch,d);return{open:c,close:n(b,g[2]),at:"open"}}}};CodeMirror.findEnclosingTag=function(a,c,d){for(var b=new h(a,c.line,c.ch,d);;){var e=r(b);if(!e)break;var f=new h(a,c.line,c.ch,d);if(f=n(f,e.tag))return{open:e,close:f}}}})();