$(function() {
  if (pointX !== undefined) {
    var s = Snap("#office_div");
    Snap.load("/static/office_plan.svg", function(data) {
      s.append(data);
      var snap = Snap("#office_map");
      var circle = snap.circle(pointX, pointY, 300);
      circle.attr({
        fill: "#ff0000",
        stroke: "#000",
        strokeWidth: 5
      });
    });
  }
});
