/* Exports a function which returns an object that overrides the default &
 *   plugin grunt configuration object.
 *
 * You can familiarize yourself with Lineman's defaults by checking out:
 *
 *   - https://github.com/linemanjs/lineman/blob/master/config/application.coffee
 *   - https://github.com/linemanjs/lineman/blob/master/config/plugins
 *
 * You can also ask Lineman's about config from the command line:
 *
 *   $ lineman config #=> to print the entire config
 *   $ lineman config concat.js #=> to see the JS config for the concat task.
 */
module.exports = function(lineman) {
	//Override application configuration here. Common examples follow in the comments.
	return {

		removeTasks:{
			common: ["jshint","handlebars"],
		},
		appendTasks:{
			dist: ["copy:local","copy:build"],
		},
		copy: {
			local: {
				files:[
						{	expand: true,
							cwd: 'app/js/local/',
							src: '**/*',
							dest: 'dist/js/local',
							}
				]	
			},
			build: {
				files:[
						{	expand: true,
							cwd: 'dist',
							src: '**/*',
							dest: '../target/classes/static',
							}
				]	
			}
		}

	};
};

